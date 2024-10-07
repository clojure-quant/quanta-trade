(ns quanta.trade.entry-signal.rule
  (:require
   [quanta.trade.entry-signal.rule.entry :as entry]
   [quanta.trade.entry-signal.rule.exit :as exit]))

(defn create-entrysignal-manager [{:keys [asset entry exit]}]
  {:positions (atom {})
   :asset asset
   :entrysize-fn (entry/positionsize2 entry)
   :exit-rules (map exit/exit-rule exit)})

(defn on-position-open [{:keys [rules positions]}  position]
  (println "rule/on-position-open: " position)
  (let [position-fn (exit/position-rules rules position)]
    (swap! positions assoc (:id position) {:position position
                                           :position-fn position-fn})))

(defn on-position-close [{:keys [positions]} position]
  (println "rule/on-position-close: " position)
  (swap! positions dissoc (:id position)))

(defn on-data [this {:keys [ds row] :as data}]
  (println "rule/on-data: " data)
  (exit/check-exit-rules this ds row))

(defn create-entry [this {:keys [ds row] :as data}]
  (println "rule/create-entry: " data)
  (entry/create-position this data))


