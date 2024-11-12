(ns dev.algo-bollinger
  (:require
   [taoensso.telemere :as tm]
   [tech.v3.datatype :as dtype]
   [tablecloth.api :as tc]
   [ta.indicator :as ind]
   [ta.indicator.band :as band]
   [ta.indicator.signal :refer [cross-up]]
   [quanta.dag.env :refer [log]]
   [quanta.bar.env :refer [get-trailing-bars]]
   [quanta.algo.dag.spec :refer [spec->ops]]
   [quanta.algo.options :refer [apply-options]]
   [quanta.trade.backtest2 :as b2]))

(defn entry-one [long short]
  (cond
    long :long
    short :short
    :else :flat))

(defn bollinger-calc [env opts bar-ds]
  (tm/log! (str "bollinger-calc dt: " bar-ds " opts: " opts))
  (when (and (= (:asset opts) "ETHUSDT")
             (= (:atr-n opts) 50))
    (log env "simulated crash eth-usdt atr-50" :bruteforce-test)
    (tm/log! "simulated crash ethusdt atr-50")
    (throw (ex-info "eth-atr-50-ex" {:message "this is used for bruteforce test"})))
  (log env "bollinger-opts: " opts)
  (let [n (or (:atr-n opts) 2)
        k (or (:atr-k opts) 1.0)
        _ (tm/log! (str "bollinger ds-bars: " bar-ds)) ; for debugging - logs to the dag logfile
        ds-bollinger (band/add-bollinger {:n n :k k} bar-ds)
        long-signal (cross-up (:close ds-bollinger) (:bollinger-upper ds-bollinger))
        short-signal (cross-up (:close ds-bollinger) (:bollinger-lower ds-bollinger))
        entry (dtype/clone (dtype/emap entry-one :keyword long-signal short-signal))
        ds-signal (tc/add-columns ds-bollinger {:entry entry
                                                :atr (ind/atr {:n n} bar-ds)})]
    ds-signal))

(def bollinger-algo
  {:* {:asset "BTCUSDT"} ; this options are global
   :bars {:calendar [:crypto :d]
          :fn get-trailing-bars
          :bardb :nippy
          :trailing-n 1100}
   :algo {:formula [:bars]
          :env? true
          :fn  bollinger-calc
          :atr-n 10
          :atr-k 0.6}
   :backtest {:formula [:algo]
              :fn b2/backtest
              :portfolio {:fee 0.05 ; per trade in percent
                          :equity-initial 10000.0}
              :entry {:type :fixed-amount :fixed-amount 100000.0}
              :exit [{:type :trailing-stop-offset :col :atr}
                     {:type :stop-prct :prct 2.0}
                     {:type :profit-prct :prct 1.0}
                     {:type :time :max-bars 10}]}})

(spec->ops bollinger-algo)
;; => [[:day {:calendar [:forex :d],
;;            :algo-fn #function[dev.bollinger-algo/bollinger-calc],
;;            :opts {:asset "BTCUSDT", :calendar [:forex :d], :trailing-n 20, :atr-n 10, :atr-m 0.6}}]
;;     [:min {:calendar [:forex :m],
;;            :algo-fn #function[dev.bollinger-algo/bollinger-calc],
;;           :opts {:asset "BTCUSDT", :calendar [:forex :m], :trailing-n 20, :atr-n 5, :atr-m 0.3}}]
;;     [:signal {:formula [:day :min],
;;               :algo-fn #function[dev.bollinger-algo/bollinger-signal],
;;               :opts {:asset "BTCUSDT", :formula [:day :min], :carry-n 2}}]]

(-> bollinger-algo
    (apply-options {[:* :asset] "ETHUSDT"
                    [:bars :calendar] [:forex :h]}))
