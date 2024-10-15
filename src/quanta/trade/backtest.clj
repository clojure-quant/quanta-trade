(ns quanta.trade.backtest
  (:require
   [tablecloth.api :as tc]
   [ta.trade.backtest.from-entry :refer [entry-signal->roundtrips]]
   [quanta.trade.report.roundtrip :refer [roundtrip-stats]]))

(defn backtest
  "uses :entry column in bar-ds to create roundtrips
   using :asset :entry :exit options.
   can be visualised with 
   quanta.viz.plot.trade.core/roundtrip-stats-ui"
  [{:keys [asset entry exit portfolio]
    :or {portfolio {:fee 0.5 ; per trade in percent
                    :equity-initial 100000.0}}
    :as opts} bar-ds]
  ; we need to get the asset from the bar-ds, because
  ; here we only see the viz-opts. this needs to be improved.
  (assert asset "backtest needs :asset option")
  (assert entry "backtest needs :entry option")
  (assert exit "backtest needs :exit option")
  (let [last-row (-> (tc/last bar-ds)
                     (tc/rows :as-maps)
                     last)
        {:keys [asset]} last-row
        backtest-opts (select-keys opts [:asset :entry :exit])
        {:keys [roundtrips exit-signal bar-entry-exit-ds] :as full} (entry-signal->roundtrips backtest-opts bar-ds)
        rt-stats (roundtrip-stats portfolio roundtrips)]
    rt-stats
    ;full
    ))
