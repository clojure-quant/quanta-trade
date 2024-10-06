(ns quanta.trade.exit)

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
    (fn [_ds {:keys [high] :as row}]
      (when (>= high target-price)
        [label target-price]))
    :short
    (fn [_ds {:keys [low] :as row}]
      (when (<= low target-price)
        [label target-price]))))

(defmethod exit-rule :profit-prct [{:keys [label prct]
                                    :or {label :take-profit-prct}
                                    :as opts}]
  (println "creating profit-prct exit rule opts: " opts)
  (assert prct "take-profit-prct needs :prct parameter")
  (fn [{:keys [price-entry side] :as position}]
    (println "creating take-profit rule for position: " position)
    (let [prct (/ prct 100.0)
          target-price (case side
                         :long (* price-entry (+ 1.0 prct))
                         :short (/ price-entry (+ 1.0 prct)))]
      (println "craeting take-profit target: " target-price)
      (take-profit (assoc opts
                          :label label
                          :target-price target-price) position))))

(defmethod exit-rule :default [{:keys [type]
                                :as opts}]
  (throw (ex-info "unkown exit-rule type" opts)))


(defn create-exit-manager [exit-rules]
  {:rules (map exit-rule exit-rules)
   :positions (atom {})})

(defn position-rules [rules position]
  (let [position-rules (map #(% position) rules)]
    (fn [ds row]
      (->> (map #(% ds row) position-rules)
           (remove nil?)
       )
      )))


(defn on-position-open [{:keys [rules positions]}  position]
  (println "on-position-open: " position)
  (let [position-fn (position-rules rules position)]
    (swap! positions assoc (:id position) {:position position 
                                           :position-fn position-fn
                                           } )))


(defn on-position-close [{:keys [positions]} position]
  (swap! positions dissoc (:id position)))
  

(defn on-bar-close [{:keys [positions]} ds row]
  (map (fn [{:keys [position 
                    position-fn]}]
         {:id (:id position)
          :asset (:asset position)
          :exit (position-fn ds row)}) (vals @positions)))






