(ns quanta.trade.commander
  (:require
   [missionary.core :as m]))

(defprotocol position-commander
  (open! [this position])
  (close! [this position]))

;(defn add-commander [dag commander]
;  (-> dag
;      (update-in [:env] assoc #'quanta.trade.commander/*commander* commander)
      ;(dag/add-cell :position-update (position-change-flow commander))
      ;(dag/add-cell :roundtrip (position-roundtrip-flow commander))
;      ))


