(ns quanta.trade.entry-signal.rule
  (:require
   [quanta.trade.entry-signal.entry.core :as entry]
   [quanta.trade.entry-signal.exit.config :refer [exit-rule]]
   [quanta.trade.entry-signal.exit.position :as exit])
  (:import
   [quanta.trade.entry_signal.exit.position MultipleRules]))

(defn create-entrysignal-manager [{:keys [asset entry exit]}]
  {:positions (atom {})
   :asset asset
   :entrysize-fn (entry/positionsize2 entry)
   :exit-rules (map exit-rule exit)})

;; entry

(defn create-entry [this data]
  ;(println "rule/create-entry: " data)
  (entry/create-position this data))

; on position open/close

(defn create-exit-manager-for-position
  "create a exit-fn for one position. 
   this gets run on each bar, while the positon is open"
  [rules position]
  ;(println "creating exit-rules for position: " position)
  ;(println "exit rules:  " rules)
  (let [position-rules (map #(% position) rules)]
    (MultipleRules. position-rules)))

(defn on-position-open
  "on-position-open is an event that gets emitted by trade-commander.
   we need to start new exit-rules for a new position here."
  [{:keys [exit-rules positions]}  position]
  (assert exit-rules "rule manager state needs to have :exit-rules")
  ;(println "rule/on-position-open: " position)
  (swap! positions assoc
         (:id position)
         {:position position
          :manager (create-exit-manager-for-position exit-rules position)}))

(defn on-position-close [{:keys [positions]} position]
  ;(println "rule/on-position-close: " position)
  (swap! positions dissoc (:id position)))

(defn check-exit-position [{:keys [position manager]} row]
  ;(println "check-exit-position: " position)
  ;; {:id 5, :asset EUR/USD, :side :long, 
  ;;         :entry-price 1.1, :qty 100000}
  (when-let [exit (exit/check-exit manager row)] ; [:profit-prct 1.1110000000000002]
    (let [[reason exit-price] exit
          {:keys [id asset side entry-price entry-date qty]} position
          {:keys [idx date]} row]
      {:id id
       :asset asset
       :side side
       :qty qty
       :entry-price entry-price
       :entry-date entry-date
     ; exit
       :reason reason
       :exit-idx idx
       :exit-price exit-price
       :exit-date date})))

(defn check-exit [{:keys [positions]} row]
  ;(println "rule/check-exit: " row)
  (->> (vals @positions)
       (map #(check-exit-position % row))
       (remove nil?)))




