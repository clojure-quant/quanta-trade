(ns quanta.trade.entry-signal.exit.position)

(defprotocol IExit
  (priority [_])
  (check-exit [_ bar])
  (get-level [_]))