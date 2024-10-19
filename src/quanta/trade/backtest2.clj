(ns quanta.trade.backtest2
  (:require
   [missionary.core :as m]
   [taoensso.telemere :as tm]
   [tablecloth.api :as tc]
   [tech.v3.dataset :as tds]
   [quanta.trade.commander :as cmd]
   [quanta.trade.backtest.commander :refer [create-position-commander roundtrips get-trades]]
   [quanta.trade.entry-signal.trader :as entry-trader]
   [quanta.trade.report.roundtrip :refer [roundtrip-stats]]))

(defn profit-level [{:keys [profit side] :as level}]
  (when (seq profit)
    ;(println "profit-level: " level)
    (case side
      :long (apply min profit)
      :short (apply max profit)
      nil)))

(defn loss-level [{:keys [loss side] :as level}]
  (when (seq loss)
    ;(println "loss-level: " level)
    (case side
      :long (apply max loss)
      :short (apply min loss)
      nil)))

(comment
  (profit-level
   [{:side :long, :profit '(101.0 105.0), :loss '(95.23809523809524)}])
  (profit-level
   [{:side :long, :profit '(), :loss '(95.23809523809524)}])
  (loss-level
   [{:side :long, :profit '(101.0 105.0), :loss '(95.23809523809524)}])
 ; 
  )

(defn entry->roundtrips [{:keys [asset entry exit] :as opts} bar-ds]
  (let [n (tc/row-count bar-ds)
        r (range n)
        _ (tm/log! (str "backtesting " asset " with bar-ds # " n))
        bar-ds-idx (tc/add-column bar-ds :idx r)
        commander (create-position-commander) ; a simplified version of a broker-api
        algo-trader (entry-trader/create-entry-trader commander opts)
        levels (doall (map (fn [row]
                             ;(tm/log! (str "row " row))
                             ; 1. process algo actions
                             (entry-trader/process-algo-action! algo-trader row)
                             ; 2. process trade-updates from commander
                             (let [trades (get-trades commander)]
                               (doall
                                (map #(entry-trader/process-position-update! algo-trader %) trades)))
                               ;(tm/log! (str "levels " levels))
                             ; 3. get stop/target levels
                             (let [levels (entry-trader/get-levels algo-trader)]
                               (first levels)))
                           (tds/mapseq-reader bar-ds-idx)))
        rts (roundtrips commander)]
    ; 
    ;(tm/log! (str "roundtrips: " rts))
    ;(tm/log! (str "levels: " levels))
    {:level-ds (tc/add-columns bar-ds {:target-profit (map profit-level levels)
                                       :target-loss (map loss-level levels)})
     :roundtrips rts}))

(defn backtest [{:keys [asset portfolio entry exit] :as opts} bar-ds]
  (let [{:keys [roundtrips]} (entry->roundtrips opts bar-ds)
        roundtrip-ds (tc/dataset roundtrips)
        rt-stats (roundtrip-stats portfolio roundtrip-ds)]
    rt-stats))








