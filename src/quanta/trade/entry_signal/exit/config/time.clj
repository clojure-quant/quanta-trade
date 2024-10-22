(ns quanta.trade.entry-signal.exit.config.time
  (:require
   [quanta.trade.entry-signal.exit.config :refer [exit-rule]]
   [quanta.trade.entry_signal.exit.position.time])
  (:import
   [quanta.trade.entry_signal.exit.position.time MaxTime]))

(defmethod exit-rule :time [{:keys [label max-bars]
                             :or {label :time}}]
  (assert max-bars "stop-time needs :max-bars parameter")
  (fn [{:keys [entry-idx] :as position}]
    (assert entry-idx "stop-time needs :position :entry-idx")
    (let [max-idx (+ entry-idx max-bars)]
      (MaxTime. position max-idx label))))
