(ns dev.algo-bollinger-backtest
  (:require
   [tick.core :as t]
   [quanta.dag.core :as dag]
   [quanta.algo.core :as algo]
   [quanta.market.barimport.bybit.import :as bybit]
   [dev.algo-bollinger :refer [bollinger-algo]]))

;; ENV

(def bar-db (bybit/create-import-bybit))

(def env {:bar-db bar-db})

(def bollinger
  (-> (dag/create-dag {:log-dir ".data/"
                       :env env})
      (algo/add-env-time-snapshot (t/instant))
      (algo/add-algo bollinger-algo)))

(dag/cell-ids bollinger)

(dag/start-log-cell bollinger :algo)

(dag/start-log-cell bollinger :backtest)

