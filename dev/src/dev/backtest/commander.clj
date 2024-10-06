(ns dev.backtest.commander
  (:require
   [tick.core :as t]
   [quanta.dag.core :as dag]
   [quanta.algo.core :as algo]
   [quanta.trade.backtest.commander :as bcommander]
   [quanta.trade.commander :as cmd]))

(def c (bcommander/create-position-commander))

(def dag
  (-> (dag/create-dag {:log-dir ".data/"
                       :env {}})
      (algo/add-env-time-snapshot (t/instant))
      (cmd/add-commander c)))

(dag/cell-ids dag)
;; => (:position-update :roundtrip)

(dag/start-log-cell dag :position-update)
(dag/start-log-cell dag :roundtrip)

(def pos1 (cmd/open! c {:asset "BTC" :side :long :qty 10 :price 5000}))
(def pos2 (cmd/open! c {:asset "BTC" :side :long :qty 10 :price 5000}))

pos1

c

(cmd/positions-snapshot c)
;; => ({:id "oCO0VF", :side :long, :qty 10, :price-entry 5000, :asset "BTC"}
;;     {:id "8bUhyG", :side :long, :qty 10, :price-entry 5000, :asset "BTC"})


(cmd/close! c (assoc pos1 :price 7000))
;; => {:asset "BTC", :side :long, :qty 10, :price 7000, :id "oCO0VF"}

(cmd/positions-snapshot c)
;; => ({:id "8bUhyG", :side :long, :qty 10, :price-entry 5000, :asset "BTC"})



