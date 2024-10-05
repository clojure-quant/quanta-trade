(ns dev.backtest.commander
  (:require
   [quanta.trade.protocol :as p]
   [quanta.trade.backtest.commander :refer [create-position-commander]]
   [dev.util :refer [start-logging]]))

(def c (create-position-commander))


c

(start-logging ".data/commander.txt" (p/position-change-flow c))
(start-logging ".data/commander.txt" (p/position-roundtrip-flow c))

(def pos1 (p/open! c {:asset "BTC" :side :long :qty 10 :price 5000}))
(def pos2 (p/open! c {:asset "BTC" :side :long :qty 10 :price 5000}))

pos1

c


(p/postions-snapshot c)


(p/close! c (assoc pos1 :price 7000))

