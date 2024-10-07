(ns dev.backtest.from-entry
  (:require
   [taoensso.timbre :as timbre :refer [info]]
   [tablecloth.api :as tc]
   [missionary.core :as m]
   [quanta.trade.backtest.backtest :refer [from-algo-cell]]
   ))


(def ds
  (tc/dataset {:open [40 40 50 80 100 240 130 70 90]
               :close [40 40 50 80 100 240 130 70 90]
               :entry [nil nil nil :long nil nil :short nil nil]}))

ds

(def algo-cell (m/seed [ds]))

(defn process-row [r
                   {:keys [idx row ds] :as x}]
  ;(info "x: "  x)
  (info "idx: " idx
        "#: "  (tc/row-count ds)
        "row: " row))

(m/? (m/reduce process-row nil (from-algo-cell algo-cell)))
