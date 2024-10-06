(ns quanta.trade.commander)

(def ^:dynamic *commander* nil)

(defprotocol position-commander
  (open! [this position])
  (close! [this position])
  (position-change-flow [this])
  (position-roundtrip-flow [this])
  (positions-snapshot [this]))
