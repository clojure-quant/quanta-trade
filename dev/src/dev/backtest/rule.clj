(ns dev.backtest.rule
  (:require
   [tick.core :as t]
   [quanta.trade.entry-signal.core :as rule]))

(def m (rule/create-entrysignal-manager
        {:asset "EUR/USD"
         :entry {:type :fixed-qty :fixed-qty 1.0}
         :exit [{:type :profit-prct :prct 1.0}
                {:type :profit-prct :prct 5.0}]}))
;; => #'dev.backtest.rule/m

m
;; => {:positions #<Atom@1938e715: {}>,
;;     :asset "EUR/USD",
;;     :entrysize-fn #function[quanta.trade.entry-signal.entry.core/eval8847/fn--8849/fn--8851],
;;     :exit-rules
;;     (#function[quanta.trade.entry-signal.exit.config/eval9125/fn--9127/fn--9130]
;;      #function[quanta.trade.entry-signal.exit.config/eval9125/fn--9127/fn--9130])}

(rule/on-position-open m  {:id 5
                           :asset "EUR/USD"
                           :side :long
                           :entry-price 1.10
                           :entry-idx 107
                           :entry-date (t/instant "2023-01-03T00:00:00Z")
                           :qty 100000})
;; => {5
;;     {:position
;;      {:id 5,
;;       :asset "EUR/USD",
;;       :side :long,
;;       :entry-price 1.1,
;;       :entry-idx 107,
;;       :entry-date #time/instant "2023-01-03T00:00:00Z",
;;       :qty 100000},
;;      :manager
;;      {:rules
;;       ({:position
;;         {:id 5,
;;          :asset "EUR/USD",
;;          :side :long,
;;          :entry-price 1.1,
;;          :entry-idx 107,
;;          :entry-date #time/instant "2023-01-03T00:00:00Z",
;;          :qty 100000},
;;         :level 1.1110000000000002,
;;         :label :profit-prct}
;;        {:position
;;         {:id 5,
;;          :asset "EUR/USD",
;;          :side :long,
;;          :entry-price 1.1,
;;          :entry-idx 107,
;;          :entry-date #time/instant "2023-01-03T00:00:00Z",
;;          :qty 100000},
;;         :level 1.1550000000000002,
;;         :label :profit-prct})}}}

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

(rule/check-exit m {:high 1.10 :low 1.09 :idx 1000 :date (t/instant)})
;; => ()

;;    ()

(rule/check-exit m {:high 1.12 :low 1.09 :idx 1001 :date (t/instant)})
;; => ({:entry-date #time/instant "2023-01-03T00:00:00Z",
;;      :entry-price 1.1,
;;      :reason :profit-prct,
;;      :exit-idx 1001,
;;      :id 5,
;;      :side :long,
;;      :qty 100000,
;;      :exit-price 1.1110000000000002,
;;      :asset "EUR/USD",
;;      :exit-date #time/instant "2024-10-10T13:08:48.806873734Z"})

(rule/check-exit m {:high 1.20 :low 1.09 :idx 1002 :date (t/instant)})
;; => ({:entry-date #time/instant "2023-01-03T00:00:00Z",
;;      :entry-price 1.1,
;;      :reason :profit-prct,
;;      :exit-idx 1002,
;;      :id 5,
;;      :side :long,
;;      :qty 100000,
;;      :exit-price 1.1110000000000002,
;;      :asset "EUR/USD",
;;      :exit-date #time/instant "2024-10-10T13:08:58.455516934Z"})

(rule/check-exit m {:high 1.20 :low 1.09 :idx 1002
                    :close 1.07 :date (t/instant) :entry :long})
;; => ({:entry-date #time/instant "2023-01-03T00:00:00Z",
;;      :entry-price 1.1,
;;      :reason :profit-prct,
;;      :exit-idx 1002,
;;      :id 5,
;;      :side :long,
;;      :qty 100000,
;;      :exit-price 1.1110000000000002,
;;      :asset "EUR/USD",
;;      :exit-date #time/instant "2024-10-10T13:09:56.032519040Z"})