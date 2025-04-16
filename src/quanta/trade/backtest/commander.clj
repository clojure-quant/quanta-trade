(ns quanta.trade.backtest.commander
  (:require
   [nano-id.core :refer [nano-id]]
   [quanta.trade.commander :as p]))

(defn check-risk [{:keys [asset-limit position-limit]} position-a p]
  (let [asset (:asset p)
        no-asset-limit? (= asset-limit 0)
        no-position-limit? (= position-limit 0)]

    (and
     ; single asset limit
     (or no-asset-limit?
         (let [asset-count (->> (vals @position-a)
                                (filter #(= asset (:asset %)))
                                count)]
           (< asset-count asset-limit)))
     ; total position limit
     (or no-position-limit?
         (let [position-count (->> (vals @position-a)
                                   count)]
           (< position-count position-limit))))))

(defn open-position! [position-a trade-a p]
  (let [p (select-keys p [:id :asset :side :qty :entry-price :entry-date :entry-idx :entry-row])]
    (swap! position-a assoc (:id p) p)
    (swap! trade-a conj {:open p})
    ;(tm/log! (str "positon open: " p))
    {:open p}))

(defn close-position! [position-a trade-a roundtrip-a exit-p]
  (let [id (:id exit-p)
        exit-p (select-keys exit-p [:exit-date :exit-idx :exit-price :exit-reason])
        pos (-> (get @position-a id)
                (dissoc :entry-row)
                (merge exit-p))]
    (swap! roundtrip-a conj pos)
    (swap! position-a dissoc id)
    (swap! trade-a conj {:close pos})
    ;(tm/log! (str "positon close: " pos))
    ;(tm/log! (str "rts: " (count @roundtrip-a)))
    {:close pos}))

(defprotocol backtest-commander
  (get-trades [_])
  (roundtrips [_]))

(defrecord position-commander [risk position-a trade-a roundtrip-a]
  backtest-commander
  (get-trades [_]
    (let [trades @trade-a]
      (reset! trade-a [])
      trades))
  (roundtrips [_]
    @roundtrip-a)
  p/position-commander
  (open! [_ {:keys [asset side qty entry-price] :as position}]
    (let [id (nano-id 6)
          position (assoc position :id id)]
      (assert asset "open-position needs :asset")
      (assert side "open-position needs :side")
      (assert qty "open-position needs :qty")
      (assert entry-price "open-position needs :entry-price")
       ;(println "commander/open! " position)
      (when (check-risk risk position-a position)
        (open-position! position-a trade-a position))))
  (close! [_ exit-position]
     ;(assert id "close-position needs :id")
     ;(assert exit-price "close-position needs :exit-price")
    ;(println "commander/close! " position)
    (close-position! position-a trade-a roundtrip-a exit-position)))

(defn create-position-commander [{:keys [asset-limit
                                         position-limit]
                                  :or {asset-limit 1
                                       position-limit 0}}]
  (let [risk {:asset-limit asset-limit
              :position-limit position-limit}
        position-a (atom {})
        trade-a (atom [])
        roundtrip-a (atom [])]
    (position-commander. risk position-a trade-a roundtrip-a)))



