(ns quanta.trade.backtest.commander
  (:require 
   [missionary.core :as m]
   [nano-id.core :refer [nano-id]]
   [quanta.trade.commander :as p]))

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
               (throw (ex-info "commander-stream-error" {}))))}))

(defrecord position-commander [positions 
                               send-action! 
                               position-action-flow
                               change-flow ]
  p/position-commander
   (open! [_ {:keys [asset side qty entry-price] :as position}]
     (let [id (nano-id 6)
           position (assoc position :id id)
           ]
       (assert asset "open-position needs :asset")
       (assert side "open-position needs :side")
       (assert qty "open-position needs :qty")
       (assert entry-price "open-position needs :entry-price")
       ;(println "commander/open! " position)
       (send-action! {:open position})
       position))
  (close! [_ {:keys [id exit-price] :as position}]
     ;(assert id "close-position needs :id")
     ;(assert exit-price "close-position needs :exit-price")
    ;(println "commander/close! " position)
    (send-action! {:close position})
    position)
  (position-change-flow [_]
    change-flow)
  (position-roundtrip-flow [_]
    (m/eduction
      (remove #(:open %))
      (map :close)
      change-flow))
  (positions-snapshot [_]
    (-> @positions vals))
  (shutdown! [_]
     (send-action! (reduced {:shutdown true}))))

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
                                                  :entry-price (:entry-price open)
                                                  :entry-date (:entry-date open)
                                                  :entry-idx (:entry-idx open)
                                                  }]
                                    (swap! positions assoc id position)
                                      {:open open})
                                  close
                                  (let [id (:id close)
                                        pos (get @positions id)
                                        pos (assoc pos :exit-price (:exit-price close)
                                                       :exit-date (:exit-date close)
                                                       :exit-idx (:exit-idx close)
                                                       :reason (:reason close)
                                                   )]
                                    (swap! positions dissoc id)
                                    {:close pos})
                                  :else 
                                   {}
                                  ))))]
    (position-commander. positions send-action! position-action-flow change-flow)))


