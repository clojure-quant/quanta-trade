(ns quanta.trade.commander
  (:require
   [quanta.dag.core :as dag]
   [missionary.core :as m]))

(def ^:dynamic *commander* nil)

(defprotocol position-commander
  (open! [this position])
  (close! [this position]))

(defn add-commander [dag commander]
  (-> dag
      (update-in [:env] assoc #'quanta.trade.commander/*commander* commander)
      ;(dag/add-cell :position-update (position-change-flow commander))
      ;(dag/add-cell :roundtrip (position-roundtrip-flow commander))
      ))


