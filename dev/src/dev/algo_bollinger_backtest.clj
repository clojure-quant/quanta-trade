(ns dev.algo-bollinger-backtest
  (:require
   [tick.core :as t]
   [quanta.dag.core :as dag]
   [quanta.algo.env.bars]
   [quanta.algo.core :refer [create-dag-live create-dag-snapshot]]
   [ta.import.provider.bybit.ds :as bybit]
   [ta.db.bars.protocol :as b]
   [ta.calendar.core :refer [trailing-window]]
   [dev.algo-bollinger :refer [bollinger-algo]]))

;; ENV

(def bar-db (bybit/create-import-bybit))
(def env {#'quanta.algo.env.bars/*bar-db* bar-db})

(def bollinger
  (create-dag-snapshot
   {:log-dir ".data/"
    :env env}
   bollinger-algo
   (t/instant)))

(dag/cell-ids bollinger)
;; => ([:crypto :d] :day [:crypto :m] :min :stats :backtest)

(dag/start-log-cell bollinger :backtest)

