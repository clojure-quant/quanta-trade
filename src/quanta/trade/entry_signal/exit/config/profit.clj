(ns quanta.trade.entry-signal.exit.config.profit
  (:require
   [quanta.trade.entry-signal.exit.config :refer [exit-rule]]
   [quanta.trade.entry_signal.exit.position.profit])
  (:import
   [quanta.trade.entry_signal.exit.position.profit TakeProfit]))

(defmethod exit-rule :profit-prct [{:keys [label prct]
                                    :or {label :profit-prct}}]
  (assert prct "take-profit-prct needs :prct parameter")
  (fn [{:keys [entry-price side] :as position}]
    ;(println "creating profit-prct rule for position: " position)
    (assert entry-price "take-profit-prct needs :position :entry-price")
    (assert side "take-profit-prct needs :position :side")
    (let [prct (/ prct 100.0)
          level (case side
                  :long (* entry-price (+ 1.0 prct))
                  :short (/ entry-price (+ 1.0 prct)))]
      (TakeProfit. position level label))))
