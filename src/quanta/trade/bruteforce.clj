(ns quanta.trade.bruteforce
  (:require
   [de.otto.nom.core :as nom]
   [tick.core :as t]
   [taoensso.timbre :refer [info warn error]]
   [missionary.core :as m]
   [nano-id.core :refer [nano-id]]
   [babashka.fs :as fs]
   [modular.fipp :refer [pprint-str]]
   [quanta.dag.core :as dag]
   [quanta.algo.core :as algo]
   [quanta.algo.options :refer [create-algo-variations]]
   [quanta.trade.backtest.store :refer [ds->nippy nippy->ds ds->transit-json-file]]))

(defn calculate-cell-once
  "creates a snapshot dag as of dt from an algo spec, 
   and calculates and returns cell-id"
  [dag-env algo-spec dt cell-id]
  (let [d (-> (dag/create-dag dag-env)
              (algo/add-env-time-snapshot dt)
              (algo/add-algo algo-spec))]
    (dag/get-current-valid-value d cell-id)))

(defn variation-keys [variations]
  (->> variations
       (partition 2)
       (map first)))

(defn get-variation [template v]
  (cond
    (keyword? v)
    [v (get template v)]

    (vector? v)
    [v (get-in template v)]

    :else
    []))

(defn summarize [algo variations]
  (->> (variation-keys variations)
       (map #(get-variation algo %))
       (into {})))

(defn run-target-fn-safe [target-fn result]
  (if (nom/anomaly? result)
    0.0
    (target-fn result)))

(defn run-show-fn-safe [show-fn result]
  (try
    (show-fn result)
    (catch Exception ex
      {})))

(defn save-ds [report-dir id ds]
  (let [fname-nippy (str report-dir id "-roundtrips.nippy.gz")
        fname-transit (str report-dir id "-roundtrips.transit-json")
        _ (ds->nippy fname-nippy ds)
        ds-safe (nippy->ds fname-nippy)]
    (ds->transit-json-file fname-transit ds-safe)))

(defn save-backtest [report-dir id backtest]
  (let [fname-nippy (str report-dir id "-backtest.nippy.gz")
        fname-transit (str report-dir id "-backtest.transit-json")
        _ (ds->nippy fname-nippy backtest)
        ds-safe (nippy->ds fname-nippy)]
    (ds->transit-json-file fname-transit ds-safe)))

(defn create-algo-task [dag-env algo cell-id dt variations target-fn show-fn report-dir]
  ; needs to throw so it can fail.
  (m/via m/cpu
         (let [id (nano-id 6)
               result (calculate-cell-once dag-env algo dt cell-id)
               summary (summarize algo variations)
               target {:target (run-target-fn-safe target-fn result)}
               show (run-show-fn-safe show-fn result)
               report (merge summary target show {:id id})]
           (when report-dir
             (spit (str report-dir id "-result.edn") (pr-str report))
             (spit (str report-dir id "-raw.txt") (with-out-str (println result)))
             (save-ds report-dir id (:roundtrip-ds result))
             (save-backtest report-dir id result))
           report)))

(defn safe-algo-one [x]
  (if (map? x)
    (dissoc x :algo)
    x))

(defn safe-algo [algo-spec]
  (if (map? algo-spec)
    (dissoc algo-spec :algo)
    (->> (map safe-algo-one algo-spec)
         (into []))))

(defn bruteforce
  "runs all variations on a template
   template-id is referring to a template that is added to quanta studio.
   mode is the algo-mode from the template that gets run
   options overrides the default options of the template (wil be done once on startup)
   the base-options for the template
   variations is a vector of [path value] tuples (partitions)
   show-fn is a fn that receives the algo-mode-result and that must return a map 
   with data that should be associated to the variation-row.
   target-fn is a value calculated from the algo-mode-result. It represents
   the value that we want to optimize (or are interested in)
   example:
   [:asset [\"BTCUSDT\" 
            \"TRXUSDT\"]
     ;:k1 [1.0 1.5]
     [:exit 1] [60 90]]
   "
  [dag-env {:keys [algo cell-id variations dt
                   target-fn show-fn
                   label
                   template-id
                   data-dir]
            :or {show-fn (fn [result] {})
                 cell-id :backtest
                 dt (t/instant)
                 template-id nil
                 data-dir ".data/bruteforce/"}}]
  ; from: https://github.com/leonoel/missionary/wiki/Rate-limiting#bounded-blocking-execution
  ; When using (via blk ,,,) It's important to remember that the blocking thread pool 
  ; is unbounded, which can potentially lead to out-of-memory exceptions. 
  ; A simple way to work around it is by using a semaphore to rate limit the execution:
  (let [report-dir (when label
                     (let [report-dir (str data-dir label "/")]
                       (fs/delete-tree report-dir)
                       (fs/create-dirs report-dir)
                       report-dir))
        sem (m/sem 10)
        algo-seq (create-algo-variations algo variations)
        tasks (map #(create-algo-task dag-env % cell-id dt variations target-fn show-fn report-dir) algo-seq)
        ;tasks-limited (map #(limit-task sem %) tasks)
        ]
    (info "brute force backtesting " (count tasks) " variations ..")
    (let [result (m/?
                  (apply m/join vector tasks))
          result (->> result
                      (sort-by :target)
                      (reverse))]
      (when label
        (let [report-filename (str data-dir label ".edn")]
          (spit report-filename
                (pprint-str {:label label
                             :template-id template-id
                             :calculated (t/instant)
                             :algo (safe-algo algo)
                             :variations variations
                             :result result}))))
      result)))

(defn cut-extension [filename]
  (subs filename 0 (- (count filename) 4)))

;; exploration

(defn show-available
  "returns a seq of labels, this are templates that have been bruteforced 
   with :label set; the results can be explored in quanta-studio."
  [dir]
  (->> (fs/list-dir dir  "*.edn")
       (map fs/file-name)
       (map cut-extension)))

(defn load-label
  "loads template-label summary for a previously generated bruteforce.
   dir must have / in the end"
  [dir label]
  (-> (str dir label ".edn")
      slurp
      read-string))


