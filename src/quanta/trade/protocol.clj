(ns quanta.trade.protocol)


(defprotocol position-commander
  (open! [this position])
  (close! [this position])
  (position-change-flow [this])
  (position-roundtrip-flow [this])
  (postions-snapshot [this])
  )
