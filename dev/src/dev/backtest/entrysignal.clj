(ns dev.backtest.entrysignal
  (:require 
   [tick.core :as t]
   [quanta.trade.entry-signal.core :as esm]))

(def m-bad (esm/create-entrysignal-manager
            {:entry nil
             :exit [{:type :glorified-dummy :a 1.0}]
             }))

m-bad

(def m (esm/create-entrysignal-manager
        {:asset "EUR/USD"
         :entry {:type :fixed-qty :fixed-qty 1.0}
         :exit [{:type :profit-prct :prct 1.0}
                {:type :profit-prct :prct 5.0}]}))

m

(esm/on-position-open m  {:id 5 
                           :asset "EUR/USD"
                           :side :long
                           :qty 100000
                           :price-entry 1.10})

(esm/on-bar-close m :ds {:high 1.10 :low 1.09 :idx 1000 :date (t/instant)})

(esm/on-bar-close m :ds {:high 1.12 :low 1.09 :idx 1001 :date (t/instant)})

(esm/on-bar-close m :ds {:high 1.20 :low 1.09 :idx 1002 :date (t/instant)})


(esm/on-bar-close m :ds {:high 1.20 :low 1.09 :idx 1002 :close 1.07 :date (t/instant) :entry :long})


(def row {:close 100.0 :entry :long
          :idx 107 :date })

(eventually-entry-position "QQQ" [:fixed-qty 3.1] row)
(eventually-entry-position "QQQ" [:fixed-amount 15000.0] row)
