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
;; => {:positions #<Atom@55aa0710: {}>,
;;     :asset "EUR/USD",
;;     :entrysize-fn #function[quanta.trade.entry-signal.rule.entry/eval40973/fn--40975/fn--40977],
;;     :exit-rules creating profit-prct exit rule opts:  {:type :profit-prct, :prct 1.0}
;;    creating profit-prct exit rule opts:  {:type :profit-prct, :prct 5.0}
;;    (#function[quanta.trade.entry-signal.rule.exit/eval49683/fn--49685/fn--49688]
;;     #function[quanta.trade.entry-signal.rule.exit/eval49683/fn--49685/fn--49688])}

(rule/on-position-open m  {:id 5 
                           :asset "EUR/USD"
                           :side :long
                           :entry-price 1.10
                           :entry-idx 107
                           :entry-date (t/instant "2023-01-03T00:00:00Z")
                           :qty 100000
                           })
;; => {5
;;     {:position
;;      {:id 5,
;;       :asset "EUR/USD",
;;       :side :long,
;;       :entry-price 1.1,
;;       :entry-idx 107,
;;       :entry-date #time/instant "2023-01-03T00:00:00Z",
;;       :qty 100000},
;;      :position-fn #function[quanta.trade.entry-signal.rule.exit/position-rules/fn--49854]}}

(rule/check-exit m {:ds nil
                 :row {:high 1.10 :low 1.09 :idx 1000 :date (t/instant)}})
;;    ()

(rule/check-exit m {:ds nil 
                 :row {:high 1.12 :low 1.09 :idx 1001 :date (t/instant)}})
;;    ({:entry-date #time/instant "2023-01-03T00:00:00Z",
;;      :entry-price 1.1,
;;      :reason :profit-prct,
;;      :exit-idx 1001,
;;      :id 5,
;;      :side :long,
;;      :qty 100000,
;;      :exit-price 1.1110000000000002,
;;      :asset "EUR/USD",
;;      :exit-date #time/instant "2024-10-07T04:14:31.986421257Z"})

(rule/check-exit m {:ds nil 
                :row {:high 1.20 :low 1.09 :idx 1002 :date (t/instant)}})
;;    ({:id 5,
;;      :asset "EUR/USD",
;;      :reason :profit-prct,
;;      :exit-idx 1002,
;;      :exit-price 1.1110000000000002,
;;      :exit-date #time/instant "2024-10-07T04:05:51.775349999Z"})

(rule/check-exit m {:ds nil
                    :row {:high 1.20 :low 1.09 :idx 1002 
                          :close 1.07 :date (t/instant) :entry :long}})

;;    ({:id 5,
;;      :asset "EUR/USD",
;;      :reason :profit-prct,
;;      :exit-idx 1002,
;;      :exit-price 1.1110000000000002,
;;      :exit-date #time/instant "2024-10-07T04:06:35.945787055Z"})



(def row {:close 100.0 :entry :long
          :idx 107 :date })

(eventually-entry-position "QQQ" [:fixed-qty 3.1] row)
(eventually-entry-position "QQQ" [:fixed-amount 15000.0] row)




(def m-bad (esm/create-entrysignal-manager
            {:entry nil
             :exit [{:type :glorified-dummy :a 1.0}]}))

m-bad
