(ns dev.backtest.backtest
  (:require
   [tick.core :as t]
   [tablecloth.api :as tc]
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
                      (t/instant "2024-09-12T00:00:00Z")]

               :open [100.0 100.0 120.0 120.0
                      100.0 100.0 120.0
                      100.0 90.0 100.0 100.0 110.0]
               :high [100.0 100.0 120.0 120.0
                      100.0 100.0 120.0
                      100.0 90.0 100.0 100.0 110.0]
               :low [100.0 100.0 120.0 120.0
                     100.0 100.0 120.0
                     100.0 90.0 100.0 100.0 110.0]
               :close [100.0 100.0 120.0 120.0
                       100.0 100.0 120.0
                       100.0 90.0 100.0 100.0 110.0]
               :atr [5.0 5.0 5.0 5.0
                     5.0 5.0 5.0
                     5.0 5.0 5.0 5.0 5.0]
               :entry [:flat :long nil nil
                       :long :flat :flat
                       :short nil nil nil]}))

bar-ds

(backtest {:asset "EUR/USD"
           :entry {:type :fixed-qty :fixed-qty 1.0}
           :exit [{:type :profit-prct :prct 1.0}
                  {:type :profit-prct :prct 5.0}]}
          bar-ds)
;; => {:roundtrip-ds _unnamed [3 21]:
;;    
;;    | :entry-idx |          :entry-date | :entry-price |      :reason | :exit-idx |    :id |  :side | :qty |  :exit-price |  :asset |           :exit-date | :cum-ret-volume | :cum-ret-prct | :bars | :win? |   :ret-abs |   :ret-log |  :ret-prct | :cum-ret-abs | :cum-ret-log |       :nav |
;;    |-----------:|----------------------|-------------:|--------------|----------:|--------|--------|-----:|-------------:|---------|----------------------|----------------:|--------------:|------:|-------|-----------:|-----------:|-----------:|-------------:|-------------:|-----------:|
;;    |          1 | 2024-09-02T00:00:00Z |        100.0 | :profit-prct |         2 | Kumqhm |  :long |  1.0 | 101.00000000 | EUR/USD | 2024-09-03T00:00:00Z |      1.00000000 |    1.00000000 |     1 |  true | 1.00000000 | 0.00432137 | 1.00000000 |   1.00000000 |   0.00432137 | 2.00432137 |
;;    |          4 | 2024-09-05T00:00:00Z |        100.0 | :profit-prct |         6 | AFcpUd |  :long |  1.0 | 101.00000000 | EUR/USD | 2024-09-07T00:00:00Z |      2.00000000 |    2.00000000 |     2 |  true | 1.00000000 | 0.00432137 | 1.00000000 |   2.00000000 |   0.00864275 | 2.00864275 |
;;    |          7 | 2024-09-08T00:00:00Z |        100.0 | :profit-prct |         8 | Iexx3q | :short |  1.0 |  99.00990099 | EUR/USD | 2024-09-09T00:00:00Z |      2.99009901 |    2.99009901 |     1 |  true | 0.99009901 | 0.00432137 | 0.99009901 |   2.99009901 |   0.01296412 | 2.01296412 |
;;    ,
;;     :metrics
;;     {:roundtrip
;;      {:avg-win-log 0.004321373782642635,
;;       :avg-bars-win 1.3333333333333333,
;;       :win-nr-prct 100.0,
;;       :pf nil,
;;       :avg-log 0.004321373782642635,
;;       :pl-log-cum 0.012964121347927904,
;;       :avg-loss-log 0.0,
;;       :trades 3,
;;       :avg-bars-loss 0.0},
;;      :nav {:cum-pl 2.9900990099009874, :max-dd 0.0}},
;;     :nav-ds _unnamed [3 6]:
;;    
;;    | :year-month-day |   :ret-log | :trades | :cum-ret-log |     :nav | :drawdown |
;;    |----------------:|-----------:|--------:|-------------:|---------:|----------:|
;;    |        20240902 | 0.00432137 |       1 |   0.00432137 | 101.0000 |       0.0 |
;;    |        20240906 | 0.00432137 |       1 |   0.00864275 | 102.0100 |       0.0 |
;;    |        20240908 | 0.00432137 |       1 |   0.01296412 | 103.0301 |       0.0 |
;;    }

(backtest {:asset "EUR/USD"
           :entry {:type :fixed-qty :fixed-qty 1.0}
           :exit [{:type :trailing-stop-offset :col :atr}]}
          bar-ds)

; |  :side | :qty | :entry-price | :exit-price |          :entry-date |           :exit-date |        :reason |
; |--------+------+--------------+-------------+----------------------+----------------------+----------------|
; |  :long |  1.0 |          100 |         115 | 2024-09-02T00:00:00Z | 2024-09-05T00:00:00Z | :trailing-stop |
; |  :long |  1.0 |          100 |         115 | 2024-09-05T00:00:00Z | 2024-09-08T00:00:00Z | :trailing-stop |
; | :short |  1.0 |          100 |         105 | 2024-09-08T00:00:00Z | 2024-09-12T00:00:00Z | :trailing-stop |
