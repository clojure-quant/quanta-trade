(ns quanta.trade.entry-signal.exit.position.loss
  (:require
   [quanta.trade.entry-signal.exit.position :refer [IExit check-exit]]))

(defrecord StopLoss [position level label]
  IExit
  (priority [_]
    1)
  (check-exit [_ {:keys [high low]}]
    (case (:side position)
      :short
      (when (>= high level)
        (let [exit-price (max low level)] ; max is in case for gaps
          [label exit-price]))
      :long
      (when (<= low level)
        (let [exit-price (min high level)] ; min is in case for gaps
          [label exit-price]))))
  (get-level [_]
    level))

(comment
  (def p {:side :long :entry-idx 1})

  (def sl (StopLoss. p 10000 :stop-loss))
  sl
  (check-exit sl {:high 9000 :low 9000 :close 9000})
  (check-exit sl {:high 11000 :low 11000 :close 11000})

;
  )
(defrecord TrailingStopLoss [position level-a new-level-fn label]
  IExit
  (priority [_]
    1)
  (check-exit [_ {:keys [high low] :as row}]
    (let [; first check if there is an exit at current level
          r (when (not (nil? @level-a))
              (case (:side position)
                :short
                (when (>= high @level-a)
                  (let [exit-price (max low @level-a)] ; max is in case for gaps
                    [label exit-price]))
                :long
                (when (<= low @level-a)
                  (let [exit-price (min high @level-a)] ; min is in case for gaps
                    [label exit-price]))))
          ; second calculate new level, and possibly move level
          unchecked-level (new-level-fn position @level-a row)
          ;_ (println "trailing unchecked-level: " unchecked-level)
          new-level (case (:side position)
                      :short
                      (when (or (nil? @level-a)
                                (< unchecked-level @level-a))
                        unchecked-level)
                      :long
                      (when (or (nil? @level-a)
                                (> unchecked-level @level-a))
                        unchecked-level))]
      (when new-level
        ;(println "trailing-stop changes from " @level-a " to: " new-level)
        (reset! level-a new-level))
      ;(when (not new-level)
        ;(println "TrailingStopLoss unchanged level: " @level-a 
        ;         " side: " (:side position) " unchecked level: " unchecked-level)
        ;)
      ;(println "trailing-stop exit: " r)
      r))
  (get-level [_]
      ;(println "trail-stop get-level: " @level-a) 
    @level-a))
