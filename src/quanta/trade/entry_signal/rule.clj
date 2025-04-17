(ns quanta.trade.entry-signal.rule
  (:require
   [quanta.trade.entry-signal.entry.core :as entry]
   [quanta.trade.entry-signal.exit.config.multiple :refer [setup-exit-rules create-exit-manager-for-position]]
   [quanta.trade.entry-signal.exit.position :as exit]))

(defn create-entrysignal-manager [{:keys [asset entry exit]}]
  {:positions (atom {})
   :asset asset
   :entrysize-fn (entry/positionsize2 entry)
   :exit-rules (setup-exit-rules exit)})

;; entry

(defn create-entry [this data]
  ;(println "rule/create-entry: " data)
  (entry/create-position this data))

; on position open/close

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
  (when (= (:asset row) (:asset position)) ; for multiple positions this is essential
    (when-let [exit (exit/check-exit manager row)] ; [:profit-prct 1.1110000000000002]
      (let [[exit-reason exit-price] exit
            {:keys [id asset side entry-price entry-date qty]} position
            {:keys [idx date]} row]
        {:id id
         :asset asset
         :side side
         :qty qty
         :entry-price entry-price
         :entry-date entry-date
     ; exit
         :exit-reason exit-reason
         :exit-idx idx
         :exit-price exit-price
         :exit-date date}))))

(defn check-exit [{:keys [positions]} row]
  ;(println "rule/check-exit: " row)
  (->> (vals @positions)
       (map #(check-exit-position % row))
       (remove nil?)))

(defn get-level-position [{:keys [position manager]}]
  (exit/get-level manager))

(defn get-levels [{:keys [positions]}]
  (->> (vals @positions)
       (map #(get-level-position %))
       (remove nil?)
       (into [])))






