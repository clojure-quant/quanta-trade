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
                      (t/instant "2024-09-10T00:00:00Z")]
               :open [100 100 120 120
                      100 100 120 
                      100 90 100]
               :high [100 100 120 120
                      100 100 120
                      100 90 100]
               :low [100 100 120 120
                     100 100 120
                     100 90 100]
               :close [100 100 120 120
                       100 100 120
                       100 90 100]
               :entry [nil :long nil nil
                       :long nil nil 
                       :short nil nil]}))

bar-ds

(backtest {:asset "EUR/USD"
           :entry {:type :fixed-qty :fixed-qty 1.0}
           :exit [{:type :profit-prct :prct 1.0}
                  {:type :profit-prct :prct 5.0}]}
          bar-ds)











