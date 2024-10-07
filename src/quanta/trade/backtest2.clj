(ns quanta.trade.backtest2
  (:require
   [tech.v3.dataset :as tds]
   [tablecloth.api :as tc]
   [missionary.core :as m]
   [quanta.trade.commander :as cmd]
   [quanta.trade.entry-signal.rule :as rule]
   [quanta.trade.backtest.commander :refer [create-position-commander]]
   [quanta.trade.backtest.from-entry :refer [from-algo-ds]]
   ))

(defn mix
  "Return a flow which is mixed by flows"
  ; will generate (count flows) processes, 
  ; so each mixed flow has its own process
  [& flows]
  (m/ap (m/?> (m/?> (count flows) (m/seed flows)))))


(defn backtest [{:keys [asset entry exit] :as opts} bar-ds]
  (let [entry-data-flow (from-algo-ds bar-ds)
        commander (create-position-commander) ; a simplified version of a broker-api
        rm (rule/create-entrysignal-manager opts)
        position-change-flow (cmd/position-change-flow commander)
        mixed-flow (mix position-change-flow entry-data-flow)
        task (m/reduce (fn [_ {:keys [data entry-signal open] :as x}]
                         (println "x: " x)
                         ;; from algo
                         (when data
                           (rule/on-data rm data))
                         (when (and data entry-signal)
                           (let [position (rule/create-entry rm data)]
                             (cmd/open! commander position)
                             ))
                         ;; from commander
                         (when open
                            ; {:side :long, :asset EUR/USD, :qty 1.0, :entry-idx 3, :entry-date nil, :entry-price 80, :id EOG7TD}
                            (rule/on-position-open rm open)
                           
                           )
                         
                         ) nil mixed-flow)]
    
    (m/? task)
    :backtest-done))



