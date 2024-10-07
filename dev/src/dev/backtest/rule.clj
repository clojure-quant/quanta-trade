(ns dev.backtest.rule
  (:require 
   [tick.core :as t]
   [quanta.trade.entry-signal.rule :as rule]))

(def m (rule/create-entrysignal-manager
        {:asset "EUR/USD"
         :entry {:type :fixed-qty :fixed-qty 1.0}
         :exit [{:type :profit-prct :prct 1.0}
                {:type :profit-prct :prct 5.0}]}))


m

(rule/on-position-open m  {:id 5 
                           :asset "EUR/USD"
                           :side :long
                           :qty 100000
                           :price-entry 1.10})
;; => {5
;;     {:position {:id 5, :asset "EUR/USD", :side :long, :qty 100000, :price-entry 1.1},
;;      :position-fn #function[quanta.trade.entry-signal.rule.exit/position-rules/fn--50880]}}

(rule/on-data m {:ds nil
                 :row {:high 1.10 :low 1.09 :idx 1000 :date (t/instant)}})

 
;; => {:exit ({:id 5, :asset "EUR/USD", :exit ()}), :entry nil}


(esm/on-bar-close m :ds {:high 1.12 :low 1.09 :idx 1001 :date (t/instant)})
;; => {:exit ({:id 5, :asset "EUR/USD", :exit ()}), :entry nil}


(esm/on-bar-close m :ds {:high 1.20 :low 1.09 :idx 1002 :date (t/instant)})
;; => {:exit ({:id 5, :asset "EUR/USD", :exit ()}), :entry nil}



(esm/on-bar-close m :ds {:high 1.20 :low 1.09 :idx 1002 :close 1.07 :date (t/instant) :entry :long})
;; => {:exit ({:id 5, :asset "EUR/USD", :exit ()}),
;;     :entry
;;     {:side :long,
;;      :asset "EUR/USD",
;;      :qty 1.0,
;;      :entry-idx 1002,
;;      :entry-date #time/instant "2024-10-06T20:34:51.898469979Z",
;;      :entry-price 1.07}}




(def row {:close 100.0 :entry :long
          :idx 107 :date })

(eventually-entry-position "QQQ" [:fixed-qty 3.1] row)
(eventually-entry-position "QQQ" [:fixed-amount 15000.0] row)




(def m-bad (esm/create-entrysignal-manager
            {:entry nil
             :exit [{:type :glorified-dummy :a 1.0}]}))

m-bad
