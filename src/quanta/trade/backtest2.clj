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

(defn entry->roundtrips [{:keys [asset entry exit] :as opts} bar-ds]
  (let [n (tc/row-count bar-ds)
        r (range n)
        bar-ds-idx (tc/add-column bar-ds :idx r)
        commander (create-position-commander) ; a simplified version of a broker-api
        algo-trader (entry-trader/create-entry-trader commander opts)]
    (tm/log! (str "backtesting " asset " with bar-ds # " n))

    (doall
     (map (fn [row]
             ;(tm/log! (str "row " row))
            (entry-trader/process-algo-action! algo-trader row)
            (let [trades (get-trades commander)]
              (doall
               (map #(entry-trader/process-position-update! algo-trader %) trades))))
          (tds/mapseq-reader bar-ds-idx)))
    (roundtrips commander)))

(defn backtest [{:keys [asset portfolio entry exit] :as opts} bar-ds]
  (let [roundtrips (entry->roundtrips opts bar-ds)
        roundtrip-ds (tc/dataset roundtrips)
        rt-stats (roundtrip-stats portfolio roundtrip-ds)]
    rt-stats))








