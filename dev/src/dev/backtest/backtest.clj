(ns dev.backtest.backtest
  (:require
   [tick.core :as t]
   [tablecloth.api :as tc]
   [quanta.trade.backtest :refer [add-asset-and-idx entry->roundtrips backtest]]))

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
               :entry [:long :long nil nil
                       :long :flat :flat
                       :short nil nil nil]}))

(def bar-entry-ds (add-asset-and-idx "EUR/USD" bar-ds))

(def opts
  {:portfolio {:fee 0.1, :equity-initial 10000.0}
   :risk {:asset-limit 1
          :position-limit 1}
   :entry {:type :fixed-qty :fixed-qty 1.0}
   :exit [{:type :profit-prct :prct 1.0}
          {:type :profit-prct :prct 5.0}
          {:type :stop-prct :prct 5.0}]})

(-> (entry->roundtrips opts bar-entry-ds)
    :level-ds)

;_unnamed [12 11]:
;|                :date | :open | :high |  :low | :close | :atr | :entry | :idx |  :asset | :target-profit | :target-loss |
;|----------------------|------:|------:|------:|-------:|-----:|--------|-----:|---------|---------------:|-------------:|
;| 2024-09-01T00:00:00Z | 100.0 | 100.0 | 100.0 |  100.0 |  5.0 |  :long |    0 | EUR/USD |   101.00000000 |  95.23809524 |
;| 2024-09-02T00:00:00Z | 100.0 | 100.0 | 100.0 |  100.0 |  5.0 |  :long |    1 | EUR/USD |   101.00000000 |  95.23809524 |
;| 2024-09-03T00:00:00Z | 120.0 | 120.0 | 120.0 |  120.0 |  5.0 |        |    2 | EUR/USD |                |              |
;| 2024-09-04T00:00:00Z | 120.0 | 120.0 | 120.0 |  120.0 |  5.0 |        |    3 | EUR/USD |                |              |
;| 2024-09-05T00:00:00Z | 100.0 | 100.0 | 100.0 |  100.0 |  5.0 |  :long |    4 | EUR/USD |   101.00000000 |  95.23809524 |
;| 2024-09-06T00:00:00Z | 100.0 | 100.0 | 100.0 |  100.0 |  5.0 |  :flat |    5 | EUR/USD |   101.00000000 |  95.23809524 |
;| 2024-09-07T00:00:00Z | 120.0 | 120.0 | 120.0 |  120.0 |  5.0 |  :flat |    6 | EUR/USD |                |              |
;| 2024-09-08T00:00:00Z | 100.0 | 100.0 | 100.0 |  100.0 |  5.0 | :short |    7 | EUR/USD |    99.00990099 | 105.00000000 |
;| 2024-09-09T00:00:00Z |  90.0 |  90.0 |  90.0 |   90.0 |  5.0 |        |    8 | EUR/USD |                |              |
;| 2024-09-10T00:00:00Z | 100.0 | 100.0 | 100.0 |  100.0 |  5.0 |        |    9 | EUR/USD |                |              |
;| 2024-09-11T00:00:00Z | 100.0 | 100.0 | 100.0 |  100.0 |  5.0 |        |   10 | EUR/USD |                |              |
;| 2024-09-12T00:00:00Z | 110.0 | 110.0 | 110.0 |  110.0 |  5.0 |        |   11 | EUR/USD |                |              |

(-> (backtest opts bar-entry-ds)
    :roundtrip-ds)
;; => _unnamed [3 29]:
;;    
;;    | :entry-idx |          :entry-date | :entry-price | :exit-idx |    :id |  :side | :qty | :exit-price | :exit-reason |  :asset |           :exit-date | :fee | :volume-trading |   :cum-log | :volume-exit | :equity | :bars | :win? | :volume-entry | :cum-prct |    :pl-log |  :pl | :pl-gross | :pl-prct | :equity-max | :pl-points | :cum-points | :drawdown | :drawdown-prct |
;;    |-----------:|----------------------|-------------:|----------:|--------|--------|-----:|------------:|--------------|---------|----------------------|-----:|----------------:|-----------:|-------------:|--------:|------:|-------|--------------:|----------:|-----------:|-----:|----------:|---------:|------------:|-----------:|------------:|----------:|---------------:|
;;    |          0 | 2024-09-01T00:00:00Z |        100.0 |         2 | RGVdaL |  :long |  1.0 |       120.0 | :profit-prct | EUR/USD | 2024-09-03T00:00:00Z |  0.2 |           220.0 | 0.07918125 |        120.0 | 10019.8 |     2 |  true |         100.0 |      19.8 | 0.07918125 | 19.8 |      20.0 |     19.8 |     10019.8 |       20.0 |        20.0 |       0.0 |            0.0 |
;;    |          4 | 2024-09-05T00:00:00Z |        100.0 |         6 | oypcUe |  :long |  1.0 |       120.0 | :profit-prct | EUR/USD | 2024-09-07T00:00:00Z |  0.2 |           220.0 | 0.15836249 |        120.0 | 10039.6 |     2 |  true |         100.0 |      39.6 | 0.07918125 | 19.8 |      20.0 |     19.8 |     10039.6 |       20.0 |        40.0 |       0.0 |            0.0 |
;;    |          7 | 2024-09-08T00:00:00Z |        100.0 |         8 | u_qqXU | :short |  1.0 |        90.0 | :profit-prct | EUR/USD | 2024-09-09T00:00:00Z |  0.2 |           190.0 | 0.20411998 |         90.0 | 10049.4 |     1 |  true |         100.0 |      49.4 | 0.04575749 |  9.8 |      10.0 |      9.8 |     10049.4 |       10.0 |        50.0 |       0.0 |            0.0 |

(def opts-2-pos
  {:portfolio {:fee 0.1, :equity-initial 10000.0}
   :risk {:asset-limit 2
          :position-limit 0}
   :entry {:type :fixed-qty :fixed-qty 1.0}
   :exit [{:type :profit-prct :prct 1.0}
          {:type :profit-prct :prct 5.0}
          {:type :stop-prct :prct 5.0}]})

(def multiple-signals-one-ds
  (-> (tc/concat bar-entry-ds bar-entry-ds bar-entry-ds)
      (tc/order-by :date)))

(-> (backtest opts-2-pos multiple-signals-one-ds)
    :roundtrip-ds)

(def usd-jpy-ds (add-asset-and-idx "USD/JPY" bar-ds))
(def gbp-jpy-ds (add-asset-and-idx "GBP/JPY" bar-ds))

(def multiple-signals-one-multiple-assets-ds
  (-> (tc/concat bar-entry-ds bar-entry-ds bar-entry-ds
                 usd-jpy-ds usd-jpy-ds usd-jpy-ds
                 gbp-jpy-ds gbp-jpy-ds gbp-jpy-ds)
      (tc/order-by :date)))

(-> (backtest opts-2-pos multiple-signals-one-multiple-assets-ds)
    :roundtrip-ds)

(def opts-1-single-3-max-pos
  {:portfolio {:fee 0.1, :equity-initial 10000.0}
   :risk {:asset-limit 1
          :position-limit 3}
   :entry {:type :fixed-qty :fixed-qty 1.0}
   :exit [{:type :profit-prct :prct 1.0}
          {:type :profit-prct :prct 5.0}
          {:type :stop-prct :prct 5.0}]})

(-> (backtest opts-1-single-3-max-pos multiple-signals-one-multiple-assets-ds)
    ;:roundtrip-ds
    ;keys
    :metrics)