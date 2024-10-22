(ns quanta.trade.entry-signal.exit.position.time
  (:require
   [quanta.trade.entry-signal.exit.position :refer [IExit check-exit]]))

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

  (def mt (MaxTime. p 5 :time))
  mt
  (check-exit mt {:idx 4})
  (check-exit mt {:idx 10 :close 5000})
  ;
  )