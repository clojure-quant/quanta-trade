(ns quanta.trade.backtest.commander
  (:require 
   [missionary.core :as m]
   [nano-id.core :refer [nano-id]]
   [quanta.trade.protocol :as p]))


(defn msg-flow [!-a]
  ; without the stream the last subscriber gets all messages
  (m/stream
   (m/observe
    (fn [!]
      (reset! !-a !)
      (fn []
        (reset! !-a nil))))))

(defn flow-sender
  "returns {:flow f
            :send s}
    (s v) pushes v to f."
  []
  (let [!-a (atom nil)]
    {:flow (msg-flow !-a)
     :send (fn [v]
             (if-let [! @!-a]
               (! v)
               (throw (ex-info "comamnder-stream-error" {}))))}))

(defrecord position-commander [positions 
                               send-action! 
                               position-action-flow
                               change-flow 
                               roundtrip-flow]
  p/position-commander
   (open! [_ {:keys [asset price side qty] :as position}]
     (let [id (nano-id 6)
           position (assoc position :id id)
           ]
       (assert asset "open-position needs :asset")
       (assert side "open-position needs :side")
       (assert qty "open-position needs :qty")
       (assert price "open-position needs :price")
       (send-action! {:open position})
       position))
  (close! [_ {:keys [id price] :as position}]
     (assert id "close-position needs :id")
     (assert price "close-position needs :price")
    (send-action! {:close position})
    position)
  (position-change-flow [_]
    change-flow)
  (position-roundtrip-flow [_]
    roundtrip-flow)
  (positions-snapshot [_]
    (-> @positions vals)))

(defn create-position-commander []
  (let [positions (atom {})
        fs (flow-sender)
        send-action! (:send fs)
        position-action-flow (:flow fs)
        change-flow  (m/stream (m/ap (let [{:keys [open close]} (m/?> position-action-flow)]
                            (cond open
                                  (let [id (:id open)
                                        position {:id id
                                                  :asset (:asset open)
                                                  :side (:side open)
                                                  :qty (:qty open)
                                                  :price-entry (:price open)}]
                                    (swap! positions assoc id position)
                                      {:open open})
                                  close
                                  (let [id (:id close)
                                        pos (get @positions id)
                                        pos (assoc pos :exit-price (:price close))]
                                    (swap! positions dissoc id)
                                    {:close pos})))))
        roundtrip-flow (m/eduction
                          (remove #(:open %))
                          (map :close)
                          change-flow)]
    (position-commander. positions send-action! position-action-flow change-flow roundtrip-flow)))

