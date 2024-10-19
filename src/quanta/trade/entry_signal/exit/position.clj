(ns quanta.trade.entry-signal.exit.position)

(defprotocol IExit
  (priority [_])
  (check-exit [_ bar])
  (get-level [_]))

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

(defrecord MaxTime [position max-idx label]
  IExit
  (priority [_]
    3)
  (check-exit [_ {:keys [idx close]}]
    (when (>= idx max-idx)
      [label close])) ; time stop is always on close.
  (get-level [_]
    nil))

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
        (println "trailing-stop changes from " @level-a " to: " new-level)
        (reset! level-a new-level))
      ;(when (not new-level)
        ;(println "TrailingStopLoss unchanged level: " @level-a 
        ;         " side: " (:side position) " unchecked level: " unchecked-level)
        ;)
      (println "trailing-stop exit: " r)
      r))
  (get-level [_]
    @level-a))

(defrecord MultipleRules [position rules]
  IExit
  (check-exit [_ {:keys [high low] :as row}]
    (->> rules
         (map #(check-exit % row))
         (remove nil?)
         first))
  (get-level [_]
    (let [rules-loss (filter #(= 1 (priority %)) rules)
          rules-profit (filter #(= 2 (priority %)) rules)
          level-loss (map #(get-level %) rules-loss)
          level-profit (map #(get-level %) rules-profit)]
      {:side (:side position)
       :profit level-profit
       :loss level-loss})))

