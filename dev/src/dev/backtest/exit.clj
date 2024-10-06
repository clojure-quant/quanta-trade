(ns dev.backtest.exit
  (:require 
     [quanta.trade.exit :as exit]))

(def m-bad (exit/create-exit-manager
        [{:type :glorified-dummy :a 1.0}]))

m-bad

(def m (exit/create-exit-manager
         [{:type :profit-prct :prct 1.0}
          {:type :profit-prct :prct 5.0}]))

m

(exit/on-position-open m  {:id 5 
                           :asset "EURUSD"
                           :side :long
                           :qty 100000
                           :price-entry 1.10})

(exit/on-bar-close m :ds {:high 1.10 :low 1.09})

(exit/on-bar-close m :ds {:high 1.12 :low 1.09})


(exit/on-bar-close m :ds {:high 1.20 :low 1.09})