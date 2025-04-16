(ns quanta.trade.entry-signal.trader
  (:require
   [missionary.core :as m]
   [quanta.trade.entry-signal.rule :as rule]
   [quanta.trade.commander :as c]))

(defn process-algo-action! [{:keys [rm commander] :as _state} algo-row]
  (let [{:keys [entry]} algo-row]
    ;; first check exits.
    (let [exits (rule/check-exit rm algo-row)]
      (when (seq exits)
        ;(tm/log! (str "exits " exits))
        (doall (map #(c/close! commander %) exits))))
    ;; second check entries.
    (when (and entry (or (= :long entry) (= :short entry)))
      ;(tm/log! (str "entry " entry))
      (let [position (rule/create-entry rm algo-row)]
        (c/open! commander position)))
    nil))

(defn process-position-update! [{:keys [rm] :as _state} {:keys [open close]}]
  (when open
       ; {:side :long, :asset EUR/USD, :qty 1.0, :entry-idx 3, :entry-date nil, :entry-price 80, :id EOG7TD}
    (rule/on-position-open rm open))
  (when close
    (rule/on-position-close rm close)))

(defn get-levels [{:keys [rm] :as _state}]
  (rule/get-levels rm))

; setup

#_(defn create-algo-action-f [{:keys [rm commander] :as state} algo-row-f]
    (m/ap
     (process-algo-action! state (m/?> algo-row-f))))

#_(defn create-position-action-f [{:keys [commander] :as state}]
    (let [position-change-flow (c/position-change-flow commander)]
      (m/ap
       (process-position-update! state (m/?> position-change-flow)))))

(defn create-entry-trader [commander opts]
  (let [rm (rule/create-entrysignal-manager opts)
        state {:rm rm :commander commander}]
    #_(assoc state
             :algo-action-flow  (create-algo-action-f state algo-row-f)
             :position-action-flow (create-position-action-f state))
    state))


