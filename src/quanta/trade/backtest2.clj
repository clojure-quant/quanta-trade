(ns quanta.trade.backtest2
  (:require
   [missionary.core :as m]
   [taoensso.telemere :as tm]
   [tablecloth.api :as tc]
   [tech.v3.dataset :as tds]
   [quanta.trade.commander :as cmd]
   [quanta.trade.backtest.commander :refer [create-position-commander roundtrips get-trades]]
   [quanta.trade.entry-signal.trader :as entry-trader]
   [quanta.trade.entry-signal.rule :refer [get-levels]]
   [quanta.trade.report.roundtrip :refer [roundtrip-stats]]))

(defn profit-level [positions]
  (when-let [p (first positions)]
    (let [{:keys [side profit]} p]
      (when (seq profit)
        (case side
          :long (apply min profit)
          :short (apply max profit)
          nil)))))

(defn loss-level [positions]
  (when-let [p (first positions)]
    (let [{:keys [side loss]} p]
      (when (seq loss)
        (case side
          :long (apply max loss)
          :short (apply min loss)
          nil)))))

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
        rule-m (:rm algo-trader)
        levels (doall (map (fn [row]
                             (tm/log! (str "row " row))
                             (entry-trader/process-algo-action! algo-trader row)
                             (let [trades (get-trades commander)]
                               (doall
                                (map #(entry-trader/process-position-update! algo-trader %) trades)))
                             (get-levels rule-m))
                           (tds/mapseq-reader bar-ds-idx)))
        rts (roundtrips commander)]
    ; 
    (tm/log! (str "roundtrips: " rts))
    (tm/log! (str "levels: " levels))
    {:level-ds (tc/add-columns bar-ds {:target-profit (map profit-level levels)
                                       :target-loss (map loss-level levels)})
     :roundtrips rts}))

(defn backtest [{:keys [asset portfolio entry exit] :as opts} bar-ds]
  (let [{:keys [roundtrips]} (entry->roundtrips opts bar-ds)
        roundtrip-ds (tc/dataset roundtrips)
        rt-stats (roundtrip-stats portfolio roundtrip-ds)]
    rt-stats))








