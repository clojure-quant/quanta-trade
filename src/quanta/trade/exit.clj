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

(defmulti exit
  "returns a closed roundtrip or nil.
   input: position + row"
  (fn [{:keys [type]} _position ] type))


(defn take-profit [{:keys [target-price label]
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

(defmethod exit :take-profit-prct [{:keys [label prct]
                                   :or {label :take-profit-prct}
                                   :as opts}
                                  {:keys [price-entry side] :as position}]
  (assert prct "take-profit-prct needs :prct parameter")
  (let [target-price (case side
                       :long (* price-entry (+ 1.0 prct))
                       :short (/ price-entry (+ 1.0 prct)))]
        (take-profit (assoc opts 
                            :label label
                            :target-price target-price
                            ) position)))






