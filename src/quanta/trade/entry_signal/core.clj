(ns quanta.trade.entry-signal.core
  (:require
   [quanta.trade.entry-signal.entry :as entry]
   [quanta.trade.entry-signal.exit :as exit]))

(defn create-entrysignal-manager [{:keys [asset entry exit]}]
  {:positions (atom {})
   :asset asset
   :entrysize-fn (entry/positionsize2 entry)
   :exit-rules (map exit/exit-rule exit)})

(defn on-position-open [{:keys [rules positions]}  position]
  (println "on-position-open: " position)
  (let [position-fn (exit/position-rules rules position)]
    (swap! positions assoc (:id position) {:position position
                                           :position-fn position-fn})))

(defn on-position-close [{:keys [positions]} position]
  (println "on-position-close: " position)
  (swap! positions dissoc (:id position)))

(defn on-bar-close [this ds row]
  (println "on-bar: " row)
  {:exit (exit/check-exit-rules this ds row)
   :entry (entry/eventually-enter-position this ds row)})
