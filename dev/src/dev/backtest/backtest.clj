(ns dev.backtest.backtest
  (:require
   [tick.core :as t]
   [taoensso.timbre :as timbre :refer [info]]
   [tablecloth.api :as tc]
   [missionary.core :as m]
   [quanta.trade.backtest2 :refer [backtest]]
   ))

(def bar-ds
  (tc/dataset {:date [(t/instant "2024-09-01T00:00:00Z")
                      (t/instant "2024-09-02T00:00:00Z")
                      (t/instant "2024-09-03T00:00:00Z")
                      (t/instant "2024-09-04T00:00:00Z")
                      (t/instant "2024-09-05T00:00:00Z")
                      (t/instant "2024-09-06T00:00:00Z")
                      (t/instant "2024-09-07T00:00:00Z")
                      (t/instant "2024-09-08T00:00:00Z")
                      (t/instant "2024-09-09T00:00:00Z")]
               :open [40 40 50 80 100 240 130 70 90]
               :close [40 40 50 80 100 240 130 70 90]
               :entry [nil nil nil :long nil nil :short nil nil]}))

(backtest {:asset "EUR/USD"
           :entry {:type :fixed-qty :fixed-qty 1.0}
           :exit [{:type :profit-prct :prct 1.0}
                  {:type :profit-prct :prct 5.0}]}
          bar-ds)




