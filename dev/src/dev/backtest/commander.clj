(ns dev.backtest.commander
  (:require
   [tick.core :as t]
   [quanta.algo.core :refer [create-dag-snapshot]]
   [quanta.trade.commander :as p]
   [quanta.trade.backtest.commander :refer [create-position-commander]]
   [dev.util :refer [start-logging]]))

(def c (create-position-commander))



(def dag 
  (create-dag-snapshot
   {:env {p/*commander* c}
    :log-dir ".data/backtest/"}
   []
   (t/instant)))

(defn 
  "creates a dag from an algo-spec
   time-events are generated once per calendar as of the date-time of 
   the last close of each calendar."
  [dag-env algo-spec dt]

c

(start-logging ".data/commander.txt" (p/position-change-flow c))
(start-logging ".data/commander.txt" (p/position-roundtrip-flow c))

(def pos1 (p/open! c {:asset "BTC" :side :long :qty 10 :price 5000}))
(def pos2 (p/open! c {:asset "BTC" :side :long :qty 10 :price 5000}))

pos1

c


(p/postions-snapshot c)


(p/close! c (assoc pos1 :price 7000))

