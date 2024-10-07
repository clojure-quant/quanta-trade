(ns quanta.trade.entry-signal.rule
  (:require
   [quanta.trade.entry-signal.rule.entry :as entry]
   [quanta.trade.entry-signal.rule.exit :as exit]))

(defn create-entrysignal-manager [{:keys [asset entry exit]}]
  {:positions (atom {})
   :asset asset
   :entrysize-fn (entry/positionsize2 entry)
   :exit-rules (map exit/exit-rule exit)})

(defn on-position-open 
  "on-position-open is an event that gets emitted by trade-commander.
   we need to start new exit-rules for a new position here."
  [{:keys [exit-rules positions]}  position]
  (assert exit-rules "rule manager state needs to have :exit-rules")
  (println "rule/on-position-open: " position)
  (let [position-fn (exit/position-rules exit-rules position)]
    (swap! positions assoc (:id position) {:position position
                                           :position-fn position-fn})))

(defn on-position-close [{:keys [positions]} position]
  (println "rule/on-position-close: " position)
  (swap! positions dissoc (:id position)))

(defn check-exit [this data]
  (println "rule/check-exit: " data)
  (exit/check-exit-rules this data))

(defn create-entry [this data]
  (println "rule/create-entry: " data)
  (entry/create-position this data))


