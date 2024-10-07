(ns dev.backtest.from-entry
  (:require
   [taoensso.timbre :as timbre :refer [info]]
   [tablecloth.api :as tc]
   [missionary.core :as m]
   [quanta.trade.backtest.from-entry :refer [from-algo-ds]]))

(def ds
  (tc/dataset {:open [40 40 50 80 100 240 130 70 90]
               :close [40 40 50 80 100 240 130 70 90]
               :entry [nil nil nil :long nil nil :short nil nil]}))

ds

(defn process-row [r {:keys [data entry-signal] :as x}]
  (info "x: "  x)
  (info "data: " data)
  (when entry-signal
    (info "entry-signal: " entry-signal)))



(m/? (m/reduce process-row nil (from-algo-ds ds)))
