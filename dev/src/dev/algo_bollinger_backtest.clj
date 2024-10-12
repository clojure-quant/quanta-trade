(ns dev.algo-bollinger-backtest
  (:require
   [tick.core :as t]
   [quanta.dag.core :as dag]
   [quanta.algo.env.bars]
   [quanta.algo.core :as algo]
   [ta.import.provider.bybit.ds :as bybit]
   [dev.algo-bollinger :refer [bollinger-algo]]))

;; ENV

(def bar-db (bybit/create-import-bybit))

(def env {#'quanta.algo.env.bars/*bar-db* bar-db})

(def bollinger
  (-> (dag/create-dag {:log-dir ".data/"
                       :env env})
      (algo/add-env-time-snapshot (t/instant))
      (algo/add-algo bollinger-algo)))

(dag/cell-ids bollinger)
;; => ([:crypto :d] :day [:crypto :m] :min :stats :backtest)

(dag/start-log-cell bollinger :day)

(dag/start-log-cell bollinger :backtest-old)

(dag/start-log-cell bollinger :backtest)

;; see .data/ for dag logfile.

