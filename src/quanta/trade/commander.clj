(ns quanta.trade.commander
  (:require
   [quanta.dag.core :as dag]
   [missionary.core :as m]))

(def ^:dynamic *commander* nil)

(defprotocol position-commander
  (open! [this position])
  (close! [this position])
  (position-change-flow [this])
  (position-roundtrip-flow [this])
  (positions-snapshot [this])
  (shutdown! [this]))

(defn add-commander [dag commander]
  (-> dag
      (update-in [:env] assoc #'quanta.trade.commander/*commander* commander)
      (dag/add-cell :position-update (position-change-flow commander))
      (dag/add-cell :roundtrip (position-roundtrip-flow commander))))

(defn start-logging [file-name flow]
  (let [print-task (m/reduce (fn [r v]
                               (let [s (with-out-str (println v))]
                                 (spit file-name s :append true))
                               nil)
                             nil flow)]
    (print-task
     #(println "flow-logger completed: " %)
     #(println "flow-logger crashed: " %))))
