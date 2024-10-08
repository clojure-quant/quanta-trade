(ns dev.backtest.backtest
  (:require
   [tick.core :as t]
   [tablecloth.api :as tc]
   [clojure.pprint :refer [print-table]]
   [quanta.trade.backtest2 :refer [backtest]]))

(def bar-ds
  (tc/dataset {:date [(t/instant "2024-09-01T00:00:00Z")
                      (t/instant "2024-09-02T00:00:00Z")
                      (t/instant "2024-09-03T00:00:00Z")
                      (t/instant "2024-09-04T00:00:00Z")
                      (t/instant "2024-09-05T00:00:00Z")
                      (t/instant "2024-09-06T00:00:00Z")
                      (t/instant "2024-09-07T00:00:00Z")
                      (t/instant "2024-09-08T00:00:00Z")
                      (t/instant "2024-09-09T00:00:00Z")
                      (t/instant "2024-09-10T00:00:00Z")
                      (t/instant "2024-09-11T00:00:00Z")
                      (t/instant "2024-09-12T00:00:00Z")
                      
                      ]
               :open [100 100 120 120
                      100 100 120 
                      100 90 100 100 110]
               :high [100 100 120 120
                      100 100 120
                      100 90 100 100 110]
               :low [100 100 120 120
                     100 100 120
                     100 90 100 100 110]
               :close [100 100 120 120
                       100 100 120
                       100 90 100 100 110]
               :atr [ 5 5 5 5
                     5 5 5
                     5 5 5 5 5]
               :entry [:flat :long nil nil
                       :long :flat :flat 
                       :short nil nil nil]}))



bar-ds

(->> (backtest {:asset "EUR/USD"
               :entry {:type :fixed-qty :fixed-qty 1.0}
               :exit [{:type :profit-prct :prct 1.0}
                      {:type :profit-prct :prct 5.0}]}
              bar-ds)

      (print-table [;  :id
                    ; :asset 
                    :side
                    :qty
                    :entry-price
                    :exit-price 
                    :entry-date
                    :exit-date 
                    :reason
                    ; :entry-idx
                    ; :exit-idx
                    ]))

; |  :side | :qty | :entry-price |       :exit-price |          :entry-date |           :exit-date |      :reason |
; |--------+------+--------------+-------------------+----------------------+----------------------+--------------|
; |  :long |  1.0 |          100 |             101.0 | 2024-09-02T00:00:00Z | 2024-09-03T00:00:00Z | :profit-prct |
; |  :long |  1.0 |          100 |             101.0 | 2024-09-05T00:00:00Z | 2024-09-07T00:00:00Z | :profit-prct |
; | :short |  1.0 |          100 | 99.00990099009901 | 2024-09-08T00:00:00Z | 2024-09-09T00:00:00Z | :profit-prct |


(->> (backtest {:asset "EUR/USD"
                :entry {:type :fixed-qty :fixed-qty 1.0}
                :exit [{:type :trailing-stop-offset :col :atr }
                       ]}
               bar-ds)
     (print-table [;  :id
                    ; :asset 
                   :side
                   :qty
                   :entry-price
                   :exit-price
                   :entry-date
                   :exit-date
                   :reason
                    ; :entry-idx
                    ; :exit-idx
                   ]))

; |  :side | :qty | :entry-price | :exit-price |          :entry-date |           :exit-date |        :reason |
; |--------+------+--------------+-------------+----------------------+----------------------+----------------|
; |  :long |  1.0 |          100 |         115 | 2024-09-02T00:00:00Z | 2024-09-05T00:00:00Z | :trailing-stop |
; |  :long |  1.0 |          100 |         115 | 2024-09-05T00:00:00Z | 2024-09-08T00:00:00Z | :trailing-stop |
; | :short |  1.0 |          100 |         105 | 2024-09-08T00:00:00Z | 2024-09-12T00:00:00Z | :trailing-stop |
