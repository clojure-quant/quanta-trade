(ns dev.backtest.backtest
  (:require
   [tick.core :as t]
   [tablecloth.api :as tc]
   [quanta.trade.backtest2 :refer [entry->roundtrips backtest]]))

(def bar-ds
  (tc/dataset {:date [(t/instant "2024-09-01T00:00:00Z")
                      (t/instant "2024-09-02T00:00:00Z")
                      (t/instant "2024-09-03T00:00:00Z")
                      (t/instant "2024-09-04T00:00:00Z")
                      (t/instant "2024-09-05T00:00:00Z")
                      (t/instant "2024-09-06T00:00:00Z")
                      (t/instant "2024-09-07T00:00:00Z")
                      (t/instant "2024-09-08T00:00:00Z")
                      (t/instant "2024-09-09T00:00:00Z")
                      (t/instant "2024-09-10T00:00:00Z")
                      (t/instant "2024-09-11T00:00:00Z")
                      (t/instant "2024-09-12T00:00:00Z")]

               :open [100.0 100.0 120.0 120.0
                      100.0 100.0 120.0
                      100.0 90.0 100.0 100.0 110.0]
               :high [100.0 100.0 120.0 120.0
                      100.0 100.0 120.0
                      100.0 90.0 100.0 100.0 110.0]
               :low [100.0 100.0 120.0 120.0
                     100.0 100.0 120.0
                     100.0 90.0 100.0 100.0 110.0]
               :close [100.0 100.0 120.0 120.0
                       100.0 100.0 120.0
                       100.0 90.0 100.0 100.0 110.0]
               :atr [5.0 5.0 5.0 5.0
                     5.0 5.0 5.0
                     5.0 5.0 5.0 5.0 5.0]
               :entry [:long :long nil nil
                       :long :flat :flat
                       :short nil nil nil]}))

bar-ds

(def opts
  {:asset "EUR/USD"
   :portfolio {:fee 0.1, :equity-initial 10000.0}
   :entry {:type :fixed-qty :fixed-qty 1.0}
   :exit [{:type :profit-prct :prct 1.0}
          {:type :profit-prct :prct 5.0}
          {:type :stop-prct :prct 5.0}]})

(-> (entry->roundtrips opts bar-ds)
    :level-ds)

; |                :date | :open | :high |  :low | :close | :atr | :entry | :target-profit | :target-loss |
; |----------------------|------:|------:|------:|-------:|-----:|--------|---------------:|-------------:|
; | 2024-09-01T00:00:00Z | 100.0 | 100.0 | 100.0 |  100.0 |  5.0 |  :flat |                |              |
; | 2024-09-02T00:00:00Z | 100.0 | 100.0 | 100.0 |  100.0 |  5.0 |  :long |   101.00000000 |  95.23809524 |
; | 2024-09-03T00:00:00Z | 120.0 | 120.0 | 120.0 |  120.0 |  5.0 |        |                |              |
; | 2024-09-04T00:00:00Z | 120.0 | 120.0 | 120.0 |  120.0 |  5.0 |        |                |              |
; | 2024-09-05T00:00:00Z | 100.0 | 100.0 | 100.0 |  100.0 |  5.0 |  :long |   101.00000000 |  95.23809524 |
; | 2024-09-06T00:00:00Z | 100.0 | 100.0 | 100.0 |  100.0 |  5.0 |  :flat |   101.00000000 |  95.23809524 |
; | 2024-09-07T00:00:00Z | 120.0 | 120.0 | 120.0 |  120.0 |  5.0 |  :flat |                |              |
; | 2024-09-08T00:00:00Z | 100.0 | 100.0 | 100.0 |  100.0 |  5.0 | :short |    99.00990099 | 105.00000000 |
; | 2024-09-09T00:00:00Z |  90.0 |  90.0 |  90.0 |   90.0 |  5.0 |        |                |              |
; | 2024-09-10T00:00:00Z | 100.0 | 100.0 | 100.0 |  100.0 |  5.0 |        |                |              |
; | 2024-09-11T00:00:00Z | 100.0 | 100.0 | 100.0 |  100.0 |  5.0 |        |                |              |
; | 2024-09-12T00:00:00Z | 110.0 | 110.0 | 110.0 |  110.0 |  5.0 |        |                |              |

(backtest opts bar-ds)
;; => {:opts {:fee 0.1, :equity-initial 10000.0}, :roundtrip-ds _unnamed [3 29]:
;;    
;;    | :entry-idx |          :entry-date | :entry-price |      :reason | :exit-idx |    :id |  :side | :qty | :exit-price |  :asset |           :exit-date | :fee | :volume-trading |   :cum-log | :volume-exit | :equity | :bars | :win? | :volume-entry | :cum-prct |    :pl-log |  :pl | :pl-gross | :pl-prct | :equity-max | :pl-points | :cum-points | :drawdown | :drawdown-prct |
;;    |-----------:|----------------------|-------------:|--------------|----------:|--------|--------|-----:|------------:|---------|----------------------|-----:|----------------:|-----------:|-------------:|--------:|------:|-------|--------------:|----------:|-----------:|-----:|----------:|---------:|------------:|-----------:|------------:|----------:|---------------:|
;;    |          1 | 2024-09-02T00:00:00Z |        100.0 | :profit-prct |         2 | LVDSSi |  :long |  1.0 |       120.0 | EUR/USD | 2024-09-03T00:00:00Z |  0.2 |           220.0 | 0.07918125 |        120.0 | 10019.8 |     1 |  true |         100.0 |      19.8 | 0.07918125 | 19.8 |      20.0 |     19.8 |     10019.8 |       20.0 |        20.0 |       0.0 |            0.0 |
;;    |          4 | 2024-09-05T00:00:00Z |        100.0 | :profit-prct |         6 | cSPQ2H |  :long |  1.0 |       120.0 | EUR/USD | 2024-09-07T00:00:00Z |  0.2 |           220.0 | 0.15836249 |        120.0 | 10039.6 |     2 |  true |         100.0 |      39.6 | 0.07918125 | 19.8 |      20.0 |     19.8 |     10039.6 |       20.0 |        40.0 |       0.0 |            0.0 |
;;    |          7 | 2024-09-08T00:00:00Z |        100.0 | :profit-prct |         8 | Oitk-W | :short |  1.0 |        90.0 | EUR/USD | 2024-09-09T00:00:00Z |  0.2 |           190.0 | 0.20411998 |         90.0 | 10049.4 |     1 |  true |         100.0 |      49.4 | 0.04575749 |  9.8 |      10.0 |      9.8 |     10049.4 |       10.0 |        50.0 |       0.0 |            0.0 |
;;    ,
;;     :metrics
;;     {:nav
;;      {:equity-final 10049.4,
;;       :cum-pl 49.400000000000006,
;;       :fee-total 0.6000000000000001,
;;       :max-drawdown 0.0,
;;       :max-drawdown-prct 0.0},
;;      :roundtrip
;;      {:pf 10.0,
;;       :win
;;       {:trades 3,
;;        :bars 4.0,
;;        :trading-volume 300.0,
;;        :pl 49.400000000000006,
;;        :pl-mean 16.46666666666667,
;;        :pl-median 19.8,
;;        :trade-prct 100.0,
;;        :bar-avg 1.3333333333333333},
;;       :loss
;;       {:trades 0, :bars 0, :trading-volume 300.0, :pl 0.0, :pl-mean 0.0, :pl-median 0.0, :trade-prct 0.0, :bar-avg 0},
;;       :all
;;       {:trades 3,
;;        :bars 4.0,
;;        :trading-volume 300.0,
;;        :pl 49.400000000000006,
;;        :pl-mean 16.46666666666667,
;;        :pl-median 19.8,
;;        :trade-prct 100.0,
;;        :bar-avg 1.3333333333333333}}}}

(backtest {:asset "EUR/USD"
           :entry {:type :fixed-qty :fixed-qty 1.0}
           :exit [{:type :trailing-stop-offset :col :atr}]}
          bar-ds)
;; => [:de.otto.nom.core/anomaly
;;     :quanta.trade.report.roundtrip/viz-calc
;;     {:message "roundtrip-stats exception!", :location :roundtrip-stats, :ex #error {
;;     :cause "Cannot invoke \"java.lang.Number.doubleValue()\" because \"x\" is null"
;;     :via
;;     [{:type java.lang.NullPointerException
;;       :message "Cannot invoke \"java.lang.Number.doubleValue()\" because \"x\" is null"
;;       :at [clojure.lang.Numbers multiply "Numbers.java" 3859]}]
;;     :trace
;;     [[clojure.lang.Numbers multiply "Numbers.java" 3859]
;;      [quanta.trade.report.roundtrip.performance$add_performance invokeStatic "performance.clj" 24]
;;      [quanta.trade.report.roundtrip.performance$add_performance invoke "performance.clj" 15]
;;      [quanta.trade.report.roundtrip$roundtrip_stats_impl invokeStatic "roundtrip.clj" 25]
;;      [quanta.trade.report.roundtrip$roundtrip_stats_impl invoke "roundtrip.clj" 20]
;;      [quanta.trade.report.roundtrip$roundtrip_stats invokeStatic "roundtrip.clj" 59]
;;      [quanta.trade.report.roundtrip$roundtrip_stats invoke "roundtrip.clj" 40]
;;      [quanta.trade.backtest2$backtest invokeStatic "NO_SOURCE_FILE" 33]
;;      [quanta.trade.backtest2$backtest invoke "NO_SOURCE_FILE" 30]
;;      [dev.backtest.backtest$eval57640 invokeStatic "NO_SOURCE_FILE" 91]
;;      [dev.backtest.backtest$eval57640 invoke "NO_SOURCE_FILE" 91]
;;      [clojure.lang.Compiler eval "Compiler.java" 7194]
;;      [clojure.lang.Compiler eval "Compiler.java" 7149]
;;      [clojure.core$eval invokeStatic "core.clj" 3216]
;;      [clojure.core$eval invoke "core.clj" 3212]
;;      [nrepl.middleware.interruptible_eval$evaluate$fn__1359$fn__1360 invoke "interruptible_eval.clj" 87]
;;      [clojure.lang.AFn applyToHelper "AFn.java" 152]
;;      [clojure.lang.AFn applyTo "AFn.java" 144]
;;      [clojure.core$apply invokeStatic "core.clj" 667]
;;      [clojure.core$with_bindings_STAR_ invokeStatic "core.clj" 1990]
;;      [clojure.core$with_bindings_STAR_ doInvoke "core.clj" 1990]
;;      [clojure.lang.RestFn invoke "RestFn.java" 425]
;;      [nrepl.middleware.interruptible_eval$evaluate$fn__1359 invoke "interruptible_eval.clj" 87]
;;      [clojure.main$repl$read_eval_print__9206$fn__9209 invoke "main.clj" 437]
;;      [clojure.main$repl$read_eval_print__9206 invoke "main.clj" 437]
;;      [clojure.main$repl$fn__9215 invoke "main.clj" 458]
;;      [clojure.main$repl invokeStatic "main.clj" 458]
;;      [clojure.main$repl doInvoke "main.clj" 368]
;;      [clojure.lang.RestFn invoke "RestFn.java" 1523]
;;      [nrepl.middleware.interruptible_eval$evaluate invokeStatic "interruptible_eval.clj" 84]
;;      [nrepl.middleware.interruptible_eval$evaluate invoke "interruptible_eval.clj" 56]
;;      [nrepl.middleware.interruptible_eval$interruptible_eval$fn__1392$fn__1396 invoke "interruptible_eval.clj" 152]
;;      [clojure.lang.AFn run "AFn.java" 22]
;;      [nrepl.middleware.session$session_exec$main_loop__1462$fn__1466 invoke "session.clj" 218]
;;      [nrepl.middleware.session$session_exec$main_loop__1462 invoke "session.clj" 217]
;;      [clojure.lang.AFn run "AFn.java" 22]
;;      [java.lang.Thread run "Thread.java" 1589]]}}]

