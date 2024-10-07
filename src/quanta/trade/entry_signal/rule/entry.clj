(ns quanta.trade.entry-signal.rule.entry)

(defmulti positionsize2
  (fn [{:keys [type] :as opts}] type))

(defmethod positionsize2 :fixed-qty
  [{:keys [type fixed-qty] :as opts}]
  (fn [_price]
    fixed-qty))

(defmethod positionsize2 :fixed-amount
  [{:keys [type fixed-amount] :as opts}]
  (fn [price]
    (/ fixed-amount price)))


(defn entry? [signal]
  (and signal ; signal might be nil
       (contains? #{:long :short} signal)))

(defn create-position [{:keys [asset entrysize-fn]} 
                       {:keys [row] :as data}]
  (let [{:keys [date idx close entry] } row]
    {:side entry
     :asset asset
     :qty (entrysize-fn close)
     :entry-idx idx
     :entry-date date
     :entry-price close}))
