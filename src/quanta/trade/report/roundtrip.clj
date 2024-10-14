(ns quanta.trade.report.roundtrip
  (:require
   [de.otto.nom.core :as nom]
   [taoensso.timbre :as timbre :refer [error]]
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
(defn- roundtrip-stats-impl [portfolio roundtrip-ds]
  (let [vr (validate-roundtrips-ds roundtrip-ds)]
    (if (nom/anomaly? vr)
      vr
      (let [;_ (println "add performance")
            roundtrip-ds (add-performance portfolio roundtrip-ds)
            ;_ (println "calc nav metrics")
            nav-metrics (nav-metrics roundtrip-ds)
            ;_ (println "calc rt metrics")
            rt-metrics (calc-roundtrip-metrics roundtrip-ds)
            ;_ (println "calc grouped nav metrics")
            ;nav-ds (grouped-nav roundtrip-ds)
            ]
        {:roundtrip-ds roundtrip-ds
         :metrics {:nav nav-metrics
                   :roundtrip rt-metrics}
         ;:nav-ds nav-ds
         }))))

(defn roundtrip-stats
  "calculate statistics for roundtrips.
   
   roundtrip-ds:  a dataset with certain requirements
   see quanta.trade.report.roundtrip.validation

   portfolio  {:fee 0.2 ; per trade in percent
               :equity-initial 10000.0}
   
   the arity with just [roundtrip-ds] is here so the code does not break.
   stop using this arity!!"
  ([roundtrip-ds]
   (roundtrip-stats {:fee 0.5 ; per trade in percent. 
                               ; fee is set very high, so to make sure code-base changes quicker
                     :equity-initial 10000.0}
                    roundtrip-ds))
  ([portfolio roundtrip-ds]
   (assert (tds/dataset? roundtrip-ds) "roundtrip stats needs a tml dataset!")
   (try
     (roundtrip-stats-impl portfolio roundtrip-ds)
     (catch Exception ex
       (error "roundtrip stat calc exception: " ex)
       (nom/fail ::viz-calc {:message "roundtrip-stats exception!"
                             :location :roundtrip-stats
                             :ex ex ; (ex-data ex)
                             })))))
