(ns quanta.trade.report.roundtrip.metrics
  (:require
   [clojure.set]
   [tablecloth.api :as tc]
   [tech.v3.dataset :as tds]
   [tech.v3.datatype.functional :as dfn]))

(defn calc-roundtrip-stats [roundtrips-ds group-by]
  (-> roundtrips-ds
      (tc/group-by group-by)
      (tc/aggregate {:bars (fn [ds]
                             (dfn/sum (:bars ds)))
                     :trades (fn [ds]
                               (tc/row-count ds))
                     ; log
                     :pl (fn [ds]
                           (dfn/sum (:pl ds)))
                     :pl-mean (fn [ds]
                                (dfn/mean (:pl ds)))
                     :pl-median (fn [ds]
                                  (dfn/median (:pl ds)))}
                    {:drop-missing? false})
      (tc/set-dataset-name (tc/dataset-name roundtrips-ds))))

(defn side-stats [roundtrip-ds]
  (calc-roundtrip-stats roundtrip-ds [:side]))

(defn win-loss-stats [roundtrips-ds]
  (calc-roundtrip-stats roundtrips-ds [:win?]))

(defn get-group-of [ds group-col group-val]
  (let [ds-filtered  (tc/select-rows ds  (fn [ds]
                                           (= group-val (group-col ds))))
        vec (into [] (tds/mapseq-reader ds-filtered))
        row (first vec)]
    row))

(defn win-loss-performance-metrics [roundtrips-ds win-loss-stats]
  (let [win (get-group-of win-loss-stats :win? true)
        loss (get-group-of win-loss-stats :win? false)
        ;_ (println "win: " win)
        ;_ (println "loss: " loss)
        ; it might be that there are no losses or no wins
        ; so we need to get defaults for nil
        ; trade #
        win {:trades (or (:trades win) 0)
             :bars (or (:bars win) 0)
             :pl (or (:pl win) 0.0)
             :pl-mean (or (:pl-mean win) 0.0)
             :pl-median (or (:pl-median win) 0.0)}
        loss {:trades (or (:trades loss) 0)
              :bars (or (:bars loss) 0)
              :pl (or  (:pl loss) 0.0)
              :pl-mean (or (:pl-mean loss) 0.0)
              :pl-median (or (:pl-median loss) 0.0)}
        all {:trades (+ (:trades win) (:trades loss))
             :bars (+ (:bars win) (:bars loss))
             :pl (+ (:pl win) (:pl loss))
             :pl-mean (dfn/mean (:pl roundtrips-ds))
             :pl-median (dfn/median (:pl roundtrips-ds))}
        ; prct
        win-prct  (let [trades-all (:trades all)]
                    (if (= 0 trades-all)
                      0
                      (* (/ (:trades win) trades-all) 100.0)))
        loss-prct (- 100.0 win-prct)
        ; bar-avg
        calc-avg-bars (fn [{:keys [trades bars]}]
                        (if (= 0 trades)
                          0
                          (/ bars trades)))
        ;; profit-factor
        pf (let [pl-win (:pl win)
                 pl-loss (:pl win)]
             (cond (= 0.0 pl-loss)
                   10.0 ; if there are no losses, return a high profit-factor
                   :else
                   (/ pl-win pl-loss)))]
    {:pf pf
     :win (assoc win :trade-prct win-prct :bar-avg (calc-avg-bars win))
     :loss (assoc loss :trade-prct loss-prct :bar-avg (calc-avg-bars loss))
     :all (assoc all :trade-prct 100.0 :bar-avg (calc-avg-bars all))}))

(defn calc-roundtrip-metrics [roundtrips-ds]
  ;(println "calc-roundtrip-metrics ..")
  (let [wl-stats (win-loss-stats roundtrips-ds)
        metrics (win-loss-performance-metrics roundtrips-ds wl-stats)]
    metrics))
