(ns quanta.trade.backtest2
  (:require
   [missionary.core :as m]
   [quanta.trade.commander :as cmd]
   [quanta.trade.entry-signal.core :as rule]
   [quanta.trade.backtest.commander :refer [create-position-commander]]
   [quanta.trade.backtest.from-entry :refer [from-algo-ds]]))


(defn algo-action [{:keys [rm commander]} entry-data-flow]
  (assert rm "algo-action needs :rm env")
  (assert rm "algo-action needs :commander env")
  (m/ap 
      (let [{:keys [data entry-signal shutdown]} (m/?> entry-data-flow)]
         (m/? (m/sleep 10))
         ;; from algo
         ;; first check exits.
         (when data
            (let [exits (rule/check-exit rm data)]
              (when (seq exits)
                (println "sending exits: " exits)  
                (doall (map #(cmd/close! commander %) exits)))))
         ;; second check entries.
         (when (and data entry-signal)
            (let [position (rule/create-entry rm data)]
              (println "sending entry: " position)
              (cmd/open! commander position)))
        
         (if shutdown
           {:shutdown true}
           data) 

        )))



 (defn batch-combiner [r v]
   (println "batch: " r v)
   (if (vector? r)
     (conj r v)
     [r v]))

(defn wrap-batch [f]
  (->> f
       (m/relieve batch-combiner)
       (m/reductions {} [])))


 (defn mix-flows [action-flow position-change-flow]
   (m/sample vector
             (wrap-batch position-change-flow)
             action-flow))

(defn backtest [{:keys [asset entry exit] :as opts} bar-ds]
  (let [entry-data-flow (from-algo-ds bar-ds)
        commander (create-position-commander) ; a simplified version of a broker-api
        rm (rule/create-entrysignal-manager opts)
        action-flow (algo-action {:rm rm :commander commander} entry-data-flow)
        position-change-flow (cmd/position-change-flow commander)
        position-change-flow (m/buffer 100 position-change-flow)
        mixed-flow (mix-flows action-flow position-change-flow)
        done (m/mbx)
        roundtrips-a (atom [])
        acc-rts-task (m/reduce (fn [r rt]
                          (println "roundtrip complete: " rt)       
                          (swap! roundtrips-a conj rt))
                               nil
                          (cmd/position-roundtrip-flow commander))
        prior-command-seq (atom [])
        task (m/reduce (fn [r x]
                         ;(println "x: " x)
                         (let [[command-seq signal-action] x]
                           ;(println "command-seq: " command-seq)
                           (when (not (= @prior-command-seq command-seq))
                             (reset! prior-command-seq command-seq)
                             (let [command-seq (if (vector? command-seq)
                                                 command-seq
                                                 [command-seq])]
                               ;(println "command-seq2: " command-seq)
                             (doall (map (fn [{:keys [open close shutdown] :as cmd-update}]
                                           ;(println "cmd-update: " cmd-update)
                                           (when open
                                           ; {:side :long, :asset EUR/USD, :qty 1.0, :entry-idx 3, :entry-date nil, :entry-price 80, :id EOG7TD}
                                             (rule/on-position-open rm open))
                                           (when close 
                                             (rule/on-position-close rm close))) 
                                         command-seq))))
                            ;; from commander
                            (println "signal-action: " signal-action)
                            (when (:shutdown signal-action)
                               (println "algo-backtest has shutdown!")
                                (done :shutdown)
                               ;  (cmd/shutdown! commander)
                              )
                           ))
                         nil mixed-flow)]

    ;(m/? task)
    (m/? (m/race task done 
                 acc-rts-task
                 ))
    @roundtrips-a
    ))








