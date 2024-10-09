(ns quanta.trade.entry-signal.rule.exit-config
(:require
  [quanta.trade.entry-signal.rule.exit2 :as e])
 (:import [quanta.trade.entry_signal.rule.exit2 TakeProfit TrailingStopLoss])  
  )

(defmulti exit-rule
  (fn [{:keys [type] :as opts}]
    type))


(defmethod exit-rule :profit-prct [{:keys [label prct]
                                    :or {label :profit-prct}}]
  (assert prct "take-profit-prct needs :prct parameter")
  (fn [{:keys [entry-price side] :as position} row]
    ;(println "creating profit-prct rule for position: " position)
    (assert entry-price "take-profit-prct needs :position :entry-price")
    (assert side "take-profit-prct needs :position :side")
    (let [prct (/ prct 100.0)
          level (case side
                  :long (* entry-price (+ 1.0 prct))
                  :short (/ entry-price (+ 1.0 prct)))]
      (TakeProfit. position level label))))

(defmethod exit-rule :trailing-stop-offset [{:keys [col label]
                                              :or {label :trailing-stop}}]
  (assert col "trailing-stop-offset needs :col parameter")
  (fn [{:keys [entry-price side] :as position}
       row]
    (assert entry-price "trailing-stop-offset needs :position :entry-price")
    (assert side "trailing-stop-offset needs :position :side")
    (let [offset (get row col)
          _ (assert offset (str "trailing-stop-offset needs :row " col " value"))
          level-initial (case side
                         :long (- entry-price offset)
                         :short (+ entry-price offset))
          level-a (atom level-initial)
          new-level-fn (fn [_position _level row]
                         (let [close (:close row)
                               offset (get row col)]
                           (case side
                             :long (- close offset)
                             :short (+ close offset))))]
      (TrailingStopLoss. position level-a new-level-fn label))))