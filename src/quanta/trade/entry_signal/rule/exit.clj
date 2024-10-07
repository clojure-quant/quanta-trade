(ns quanta.trade.entry-signal.rule.exit)

;; on-position-open: (exit opts position)
;; which returns: (fn [ds row])
;;
;; an exit rule gets created when a position is opened.
;;
;; the opts parameter is a map, whose :type key is mandatory
;; and is used to construct different exti-rule-types.
;; stateful exit rules, can create a state atom when
;; a position is opened.
;; a :label parameter can be passed, this is used to identify
;; the exit reason, by default it is set to the :type, as
;; we assume in the simplest scenario there is only one 
;; :type :profit-target for example
;;
;; (fn [ds row])
;; for each bar the position is open this fn is called
;; ds is the dataset up to the current bar, row is
;; the last row of the dataset as a map.

(defmulti exit-rule
  "returns a closed roundtrip or nil.
   input: position + row"
  (fn [{:keys [type]}] type))


(defn- take-profit [{:keys [target-price label]
                     :or {label :take-profit}
                     :as opts}
                    {:keys [side] :as position}]
  (case side
    :long
    (fn [{:keys [row]}]
      ;(println "take-profit-long check: " row)
      (let [{:keys [high]} row]
        (when (>= high target-price)
          [label target-price])))
    :short
    (fn [{:keys [row]}]
      ;(println "take-profit-short check: " row)
      (let [{:keys [low]} row]
        (when (<= low target-price)
          [label target-price])))))

(defmethod exit-rule :profit-prct [{:keys [label prct]
                                    :or {label :profit-prct}
                                    :as opts}]
  ;(println "creating profit-prct exit rule opts: " opts)
  (assert prct "take-profit-prct needs :prct parameter")
  (fn [{:keys [entry-price side] :as position}]
    ;(println "creating profit-prct rule for position: " position)
    (assert entry-price "exit-rule needs :entry-price")
    (assert side "exit-rule needs :side")
    (let [prct (/ prct 100.0)
          target-price (case side
                         :long (* entry-price (+ 1.0 prct))
                         :short (/ entry-price (+ 1.0 prct)))]
      ;(println "take-profit target: " target-price)
      (take-profit (assoc opts
                          :label label
                          :target-price target-price) position))))

(defmethod exit-rule :default [{:keys [type]
                                :as opts}]
  (throw (ex-info "unkown exit-rule type" opts)))

(defn position-rules 
  "create a exit-fn for one position. 
   this gets run on each bar, while the positon is open"
  [rules position]
  ;(println "creating exit-rules for position: " position)
  ;(println "exit rules:  " rules)
  (let [position-rules (map #(% position) rules)]
    (fn [data]
      (->> (map #(% data) position-rules)
           (remove nil?)
           first))))


(defn check-exit-position [{:keys [position
                                   position-fn]
                            :as p} data]
  ;(println "check-exit-position: " position)
  ;; {:id 5, :asset EUR/USD, :side :long, 
  ;;         :entry-price 1.1, :qty 100000}

  (when-let [exit (position-fn data)] ; [:profit-prct 1.1110000000000002]
    (let [[reason exit-price] exit
          {:keys [id asset side entry-price entry-date qty]} position
          {:keys [idx date]} (:row data)
          ]
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
     :exit-date date
     })))

(defn check-exit-rules [{:keys [positions]} data]
  (->> (vals @positions)
       (map #(check-exit-position % data))
       (remove nil?)))



