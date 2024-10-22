(ns quanta.trade.entry-signal.exit.position.profit
  (:require
   [quanta.trade.entry-signal.exit.position :refer [IExit check-exit]]))

(defrecord TakeProfit [position level label]
  IExit
  (priority [_]
    2)
  (check-exit [_ {:keys [open high low]}]
    (case (:side position)
      :long
      (when (>= high level)
        (let [exit-price (if (>= open level) ; if triggered on open, close on open
                           open            ; this is the gap case
                           level)]
          [label exit-price]))
      :short
      (when (<= low level)
        (let [exit-price (if (<= open level)
                           open
                           level)]
          [label exit-price]))))
  (get-level [_]
    level))

(comment
  (def p {:side :long :entry-idx 1})

  (def tp (TakeProfit. p 10000 :take-profit))
  tp
  (check-exit tp {:high 9000 :low 9000 :close 9000})
  (check-exit tp {:high 11000 :low 11000 :close 11000})

;
  )
(defrecord TrailingTakeProfit [position level adjust-level-fn label]
  IExit
  (priority [_]
    2)
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
        ;(println "TrailingTakeProfit changes to: " level)
        (reset! level new-level))
      r))
  (get-level [_]
    @level))