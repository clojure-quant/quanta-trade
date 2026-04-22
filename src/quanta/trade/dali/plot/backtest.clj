(ns quanta.trade.dali.plot.backtest
  (:require
   [dali.spec :refer [create-dali-spec]]))

(defn metrics [metrics]
  (create-dali-spec
   {:viewer-fn 'quanta.trade.dali.viewer.backtest/metrics
    :data metrics}))

(defn table [roundtrip-ds]
  (create-dali-spec
   {:viewer-fn 'quanta.trade.dali.viewer.backtest/table
    :data roundtrip-ds}))

(defn chart [roundtrip-ds]
  (create-dali-spec
   {:viewer-fn 'quanta.trade.dali.viewer.backtest/chart
    :data roundtrip-ds}))

(defn backtest [backtest-result]
  (create-dali-spec
   {:viewer-fn 'quanta.trade.dali.viewer.backtest/backtest
    :data {:data backtest-result}}))

(defn overview [backtest-result]
  (create-dali-spec
   {:viewer-fn 'quanta.trade.dali.viewer.backtest/overview
    :data {:result backtest-result}}))



