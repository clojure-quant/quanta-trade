(ns quanta.trade.entry-signal.exit.config.profit
  (:require
   [quanta.trade.entry-signal.exit.config :refer [exit-rule]]
   [quanta.trade.entry_signal.exit.position.profit])
  (:import
   [quanta.trade.entry_signal.exit.position.profit TakeProfit TrailingTakeProfit]))

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

(defmethod exit-rule :trailing-profit-level [{:keys [col-long col-short label]
                                              :or {label :trailing-profit-level}}]
  (assert col-long "trailing-profit-target needs :col-long parameter")
  (assert col-short "trailing-profit-target needs :col-short parameter")
  (fn [position]
    ;(println "trailing-profit-level created for : " position)
    (let [{:keys [side entry-row]} position
          level-initial (case side
                          :long  (get entry-row col-long)
                          :short  (get entry-row col-short))
          ; _ (println "trailing-stop initial: side: " side " entry-price: " entry-price "offet: " offset " level: " level-initial)
          level-a (atom level-initial)
          new-level-fn (fn [position _level row]
                         (let [{:keys [side]} position
                               level-new (case side
                                           :long (get row col-long)
                                           :short (get row col-short))]
                           ; (println "trailing-stop side:"  side " offset: " offset " close: " close " level: " level-new)
                           level-new))]
      (TrailingTakeProfit. position level-a new-level-fn label))))
