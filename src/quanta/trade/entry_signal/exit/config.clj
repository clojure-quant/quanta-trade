(ns quanta.trade.entry-signal.exit.config
  (:require
   [quanta.trade.entry-signal.exit.position :as e])
  (:import
   [quanta.trade.entry_signal.exit.position
    TakeProfit
    StopLoss TrailingStopLoss
    MaxTime
    MultipleRules]))

(defmulti exit-rule
  (fn [{:keys [type] :as opts}]
    type))

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

(defmethod exit-rule :stop-prct [{:keys [label prct]
                                  :or {label :stop-prct}}]
  (assert prct "stop-prct needs :prct parameter")
  (fn [{:keys [entry-price side] :as position}]
    ;(println "creating profit-prct rule for position: " position)
    (assert entry-price "stop-prct needs :position :entry-price")
    (assert side "stop-prct needs :position :side")
    (let [prct (/ prct 100.0)
          level (case side
                  :long (/ entry-price (+ 1.0 prct))
                  :short (* entry-price (+ 1.0 prct)))]
      (StopLoss. position level label))))

(defmethod exit-rule :time [{:keys [label max-bars]
                             :or {label :time}}]
  (assert max-bars "stop-time needs :max-bars parameter")
  (fn [{:keys [entry-idx] :as position}]
    (assert entry-idx "stop-time needs :position :entry-idx")
    (let [max-idx (+ entry-idx max-bars)]
      (MaxTime. position max-idx label))))

(defmethod exit-rule :trailing-stop-offset [{:keys [col label]
                                             :or {label :trailing-stop}}]
  (assert col "trailing-stop-offset needs :col parameter")
  (fn [position]
    ;(assert entry-price "trailing-stop-offset needs :position :entry-price")
    ;(assert side "trailing-stop-offset needs :position :side")
    (let [;_ (assert offset (str "trailing-stop-offset needs :row " col " value"))
          {:keys [entry-price side entry-row]} position
          offset (get entry-row col)
          level-initial (case side
                          :long (- entry-price offset)
                          :short (+ entry-price offset))
          ; _ (println "trailing-stop initial: side: " side " entry-price: " entry-price "offet: " offset " level: " level-initial)
          level-a (atom level-initial)
          new-level-fn (fn [position level row]
                         (let [{:keys [side]} position
                               close (:close row)
                               offset (get row col)
                               level-new (case side
                                           :long (- close offset)
                                           :short (+ close offset))]
                           ; (println "trailing-stop side:"  side " offset: " offset " close: " close " level: " level-new)
                           level-new))]
      (TrailingStopLoss. position level-a new-level-fn label))))

(defn setup-exit-rules [exit]
  (map exit-rule exit))