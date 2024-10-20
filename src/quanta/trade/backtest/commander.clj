(ns quanta.trade.backtest.commander
  (:require
   [taoensso.telemere :as tm]
   [nano-id.core :refer [nano-id]]
   [quanta.trade.commander :as p]))

(defn has-position-in [position-a p]
  (let [asset (:asset p)]
    (some #(= asset (:asset %)) (vals @position-a))))

(defn open-position! [position-a trade-a p]
  (when (not (has-position-in position-a p))
    (let [p (select-keys p [:id :asset :side :qty :entry-price :entry-date :entry-idx :entry-row])]
      (swap! position-a assoc (:id p) p)
      (swap! trade-a conj {:open p})
    ;(tm/log! (str "positon open: " p))
      {:open p})))

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

(defrecord position-commander [position-a trade-a roundtrip-a]
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
      (open-position! position-a trade-a position)
      position))
  (close! [_ exit-position]
     ;(assert id "close-position needs :id")
     ;(assert exit-price "close-position needs :exit-price")
    ;(println "commander/close! " position)
    (close-position! position-a trade-a roundtrip-a exit-position)))

(defn create-position-commander []
  (let [position-a (atom {})
        trade-a (atom [])
        roundtrip-a (atom [])]
    (position-commander. position-a trade-a roundtrip-a)))



