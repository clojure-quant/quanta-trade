(ns quanta.trade.entry-signal.rule.exit2)

(defprotocol IExit
  (check-exit [_ bar]))

(defrecord TakeProfit [position level label]
  IExit
  (check-exit [_ {:keys [high low]}]
    (case (:side position)
      :long
      (when (>= high level)
        [label level])
      :short
      (when (<= low level)
        [label level]))))

(defrecord StopLoss [position level label]
  IExit
  (check-exit [_ {:keys [high low]}]
    (case (:side position)
      :short
      (when (>= high level)
        [label level])
      :long
      (when (<= low level)
        [label level]))))

(defrecord MaxTime [position max-idx label]
  IExit
  (check-exit [_ {:keys [idx close]}]
    (when (>= idx max-idx)
      [label close])))

(comment
  (def p {:side :long :entry-idx 1})

  (def tp (TakeProfit. p 10000 :take-profit))
  tp
  (check-exit tp {:high 9000 :low 9000 :close 9000})
  (check-exit tp {:high 11000 :low 11000 :close 11000})

  (def sl (StopLoss. p 10000 :stop-loss))
  sl
  (check-exit sl {:high 9000 :low 9000 :close 9000})
  (check-exit sl {:high 11000 :low 11000 :close 11000})

  (def mt (MaxTime. p 5 :time))
  mt
  (check-exit mt {:idx 4})
  (check-exit mt {:idx 10 :close 5000})
  ;
  )

(defmulti exit-rule
  (fn [{:keys [type]}] type))

(defmethod exit-rule :profit-prct [{:keys [position label prct]
                                    :or {label :profit-prct}}]
  (assert prct "take-profit-prct needs :prct parameter")
  (assert prct "take-profit-prct needs :position parameter")
  (let [{:keys [entry-price side]} position]
    ;(println "creating profit-prct rule for position: " position)
    (assert entry-price "take-profit-prct needs :position :entry-price")
    (assert side "take-profit-prct needs :position :side")
    (let [prct (/ prct 100.0)
          level (case side
                  :long (* entry-price (+ 1.0 prct))
                  :short (/ entry-price (+ 1.0 prct)))]
      (TakeProfit. position level label))))

(defrecord TrailingTakeProfit [position level adjust-level-fn label]
  IExit
  (check-exit [_ {:keys [high low] :as row}]
    (let [r (case (:side position)
              :long
              (when (>= high @level)
                [label level])
              :short
              (when (<= low @level)
                [label level]))
          new-level (adjust-level-fn position level row)]
      (when new-level
        (println "TrailingTakeProfit changes to: " level)
        (reset! level new-level))
      r)))

(defrecord TrailingStopLoss [position level adjust-level-fn label]
  IExit
  (check-exit [_ {:keys [high low] :as row}]
    (let [r (case (:side position)
              :short
              (when (>= high level)
                [label level])
              :long
              (when (<= low level)
                [label level]))
          new-level (adjust-level-fn position level row)]
      (when new-level
        (println "TrailingStopLoss changes to: " level)
        (reset! level new-level))
      r)))