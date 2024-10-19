(ns quanta.trade.entry-signal.entry.core)

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
                       {:keys [date idx close entry] :as row}]
  {:side entry
   :asset asset
   :qty (entrysize-fn close)
   :entry-idx idx
   :entry-date date
   :entry-price close
   :entry-row row ; this is important for dynamic exit rule creation.
   })
