(ns dev.exit.tests
  (:require
   [quanta.trade.exit-helper :refer [long-exit-prices]]))

;; TARGET PRCT no gap

(long-exit-prices
 {:offset {; offsets create from a price a bar by adding each offset
           :open -0.1
           :high 0.2
           :low -0.2
           :close 0.0}
  :exit  [{:type :profit-prct :prct 1.0}]
  :price  [100.0 80.0 100.9 100.9 101.9 80.5]})
;; => [nil nil [:profit-prct 101.0] [:profit-prct 101.0] [:profit-prct 101.80000000000001] nil]

;; => [nil nil [:profit-prct 101.0] [:profit-prct 101.0] [:profit-prct 101.0] nil]
; 100.9 + 0.2 = 101.1

;; TARGET PRCT gap

(long-exit-prices
 {:offset {; offsets create from a price a bar by adding each offset
           :open -0.1
           :high 0.2
           :low -0.2
           :close 0.0}
  :exit  [{:type :profit-prct :prct 1.0}]
  :price  [100.0 80.0 120.9 100.9 101.9 80.5]})
;; => [nil nil [:profit-prct 120.80000000000001] [:profit-prct 101.0] [:profit-prct 101.80000000000001] nil]

;; => [nil nil [:profit-prct 120.80000000000001] [:profit-prct 101.0] [:profit-prct 101.80000000000001] nil]
; 120.9 - 0.1 = 120.8 closes at open
; 100.9 - 0.1 = 100.8 (below limit) high: 100.9+0.2 = 101.1 closes at limit
; 101.9 - 0.1 = 101.8 closes at open

;; STOP PRCT no gap

(long-exit-prices
 {:offset {:open -0.1
           :high 0.2
           :low -0.2
           :close 0.0}
  :exit  [{:type :stop-prct :prct 2.0}]
  :price  [100.0 98.0 100.9 100.9 101.9 80.5]})
;; => [nil [:stop-prct 98.0392156862745] nil nil nil [:stop-prct 80.7]]
(/ 100.0 1.02)
;; => 98.0392156862745

;; STOP PRCT with gap

(long-exit-prices
 {:offset {:open -0.1
           :high 0.2
           :low -0.2
           :close 0.0}
  :exit  [{:type :stop-prct :prct 2.0}]
  :price  [100.0 80.0 100.9 100.9 101.9]})
;; => [nil [:stop-prct 80.2] nil nil nil]
;; stop with gap, we get executed below our stop.

;; STOP TIME

(long-exit-prices
 {:offset {:open -0.1
           :high 0.2
           :low -0.2
           :close 0.0}
  :exit  [{:type :time :max-bars 5}]
  :price  [100.0 80.0 100.9 100.9 101.9 105.3 20.5]})
;; => [nil nil nil nil [:time 101.9] [:time 105.3] [:time 20.5]]
; always closes at close after 5 bars.

;; TRAILING STOP OFFSET NO GAP

(long-exit-prices
 {:offset {:open -0.1
           :high 0.2
           :low -0.2
           :close 0.0}
  :exit [{:type :trailing-stop-offset :col :atr}]
  :price [100.0 104.0 110.0 108.4]
  :col {:atr [1.0 0.8 1.5 1.9]
        :xyz [5.0 5.8 5.5 5.9]}})
;; => [nil nil nil [:trailing-stop 108.5]]
;; at 110.0 close atr was 1.5 -> stop 108.5

;; TRAILING STOP OFFSET GAP

(long-exit-prices
 {:offset {:open -0.1
           :high 0.2
           :low -0.2
           :close 0.0}
  :exit [{:type :trailing-stop-offset :col :atr}]
  :price [100.0 104.0 110.0 105.0]
  :col {:atr [1.0 0.8 1.5 1.9]
        :xyz [5.0 5.8 5.5 5.9]}})
;; => [nil nil nil [:trailing-stop 105.2]]
;; at 110.0 close atr was 1.5 -> stop 108.5
;; but entire bar is below 108.5 so closes at high = 105.0 + 0.2 = 105.2

;; MULTIPLE EXITS

(long-exit-prices
 {:offset {:open -0.1
           :high 0.2
           :low -0.2
           :close 0.0}
  :exit [{:type :profit-prct :prct 1.0}
         {:type :stop-prct :prct 5.0}
         {:type :time :max-bars 5}
         {:type :trailing-stop-offset :col :atr}]
  :price [100.0 80.0 100.5 100.9 101.9
          100.1 100.2 100.3]
  :col {:atr [1.0 0.8 1.5 1.9 1.9
              1.1 1.2 1.3]
        :xyz [5.0 5.8 5.5 5.9 5.9
              5.1 5.2 5.3]}})
;; => [nil [:stop-prct 95.23809523809524] nil nil [:profit-prct 101.0] 
;;    [:time 100.1] [:time 100.2] [:time 100.3]]

; trailing profit level

(long-exit-prices
 {:offset {:open -0.1
           :high 0.2
           :low -0.2
           :close 0.0}
  :exit [{:type :trailing-profit-level :col-long :target :col-short :target}]
  :price [100.0 80.0 100.5 100.9 101.9
          100.1 100.2 100.3]
  :col {:atr [1.0 0.8 1.5 1.9 1.9
              1.1 1.2 1.3]
        :target [101.5 105.0 110.0 101.7 101.9
                 101.10 101.20 101.30]}})
;; => [nil nil nil nil [:trailing-profit-level 101.7] nil nil nil]



