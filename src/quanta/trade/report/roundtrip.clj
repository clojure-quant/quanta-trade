(ns quanta.trade.report.roundtrip
  (:require
   [tech.v3.dataset :as tds]
   [tech.v3.datatype.functional :as dfn]
   [quanta.trade.report.roundtrip.validation :refer [validate-roundtrips-ds]]
   [quanta.trade.report.roundtrip.performance :refer [add-performance]]
   [quanta.trade.report.roundtrip.metrics :refer [calc-roundtrip-metrics]]
   ;[quanta.trade.report.roundtrip.nav.grouped :refer [grouped-nav]]
   ))

(defn nav-metrics [{:keys [equity drawdown drawdown-prct pl fee]}]
  {:equity-final (last equity)
   :cum-pl (dfn/sum pl)
   :fee-total (dfn/sum fee)
   :max-drawdown (apply dfn/max drawdown)
   :max-drawdown-prct (apply dfn/max drawdown-prct)})

(defn roundtrip-stats
  "calculate statistics for roundtrips.
     
     roundtrip-ds:  a dataset with certain requirements
     see quanta.trade.report.roundtrip.validation
  
     portfolio  {:fee 0.2 ; per trade in percent
                 :equity-initial 10000.0}"
  [portfolio roundtrip-ds]
  (assert portfolio "roundtrip stats needs portfolio parameter!")
  (assert (tds/dataset? roundtrip-ds) "roundtrip stats needs a tml dataset!")
  (validate-roundtrips-ds roundtrip-ds) ; will throw if not validated
  (let [;_ (println "add performance")
        roundtrip-ds (add-performance portfolio roundtrip-ds)
            ;_ (println "calc nav metrics")
        nav-metrics (nav-metrics roundtrip-ds)
            ;_ (println "calc rt metrics")
        rt-metrics (calc-roundtrip-metrics roundtrip-ds)
            ;_ (println "calc grouped nav metrics")
            ;nav-ds (grouped-nav roundtrip-ds)
        ]
    {:opts portfolio
     :roundtrip-ds roundtrip-ds
     :metrics {:nav nav-metrics
               :roundtrip rt-metrics}
              ;:nav-ds nav-ds
     }))