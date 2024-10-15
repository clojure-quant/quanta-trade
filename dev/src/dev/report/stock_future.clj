(ns report.stock-future
  (:require
   [clojure.pprint :refer [print-table]]
   [tick.core :as t]
   [tablecloth.api :as tc]
   [clojure.edn :as edn]
   [quanta.trade.report.roundtrip :refer [roundtrip-stats]]
   [cquant.tmlds :refer [ds->transit-json-file]]))

;; load a roundtrip dataset from a file
;; the file has date as string, so we need to clean before using it.

(defn dt [sdt]
  (-> sdt
      t/date
      (t/at (t/time "00:00:00"))
      (t/instant)))

(defn clean [row]
  (-> row
      (update :entry-date dt)
      (update :exit-date dt)))

(defn days-difference [date1 date2]
  (-> (t/units (t/between date1 date2))
      :seconds
      (/ 86400)))

(defn add-bar-index [roundtrips]
  (let [{:keys [entry-date]} (first roundtrips)
        dt0 entry-date]
    (map (fn [{:keys [entry-date exit-date] :as rt}]
           (assoc rt :entry-idx (days-difference dt0 entry-date)
                  :exit-idx (days-difference dt0 exit-date)))
         roundtrips)))

(defn load-roundtrips []
  (->> (slurp "src/dev/demodata/roundtrips-stock-future.edn")
       (edn/read-string)
       (map clean)
       (add-bar-index)))

(-> (load-roundtrips)
    print-table)

(def roundtrip-ds
  (-> (load-roundtrips)
      (tc/dataset)))

roundtrip-ds

(def report
  (roundtrip-stats
   {:fee 0.05 ; per trade in percent. 
    :equity-initial 100000.0}
   roundtrip-ds))

(keys report)
;; => (:opts :roundtrip-ds :metrics)

(:opts report)
;; => {:fee 0.05, :equity-initial 100000.0}

(:metrics report)
;; => {:nav
;;     {:equity-final 276505.53018256003,
;;      :cum-pl 176505.53018256003,
;;      :fee-total 10888.30325744,
;;      :max-drawdown 73071.88,
;;      :max-drawdown-prct 36.339486510132666},
;;     :roundtrip
;;     {:pf 3.4027126549505087,
;;      :win
;;      {:trades 170,
;;       :bars 3179.0,
;;       :trading-volume 1.034406325744E7,
;;       :pl 249966.47018256004,
;;       :pl-mean 1470.3910010738825,
;;       :pl-median 895.1,
;;       :trade-prct 94.97206703910615,
;;       :bar-avg 18.7},
;;      :loss
;;      {:trades 9,
;;       :bars 485.0,
;;       :trading-volume 1.034406325744E7,
;;       :pl -73460.94,
;;       :pl-mean -8162.326666666667,
;;       :pl-median -91.05,
;;       :trade-prct 5.02793296089385,
;;       :bar-avg 53.888888888888886},
;;      :all
;;      {:trades 179,
;;       :bars 3664.0,
;;       :trading-volume 1.034406325744E7,
;;       :pl 176505.53018256003,
;;       :pl-mean 986.0644144277097,
;;       :pl-median 817.47,
;;       :trade-prct 100.0,
;;       :bar-avg 20.46927374301676}}}

(* 8648247.13744 0.05 0.01 2.0)
;; => 8648.247137440001

(ds->transit-json-file
 roundtrip-ds
 "src/dev/demodata/roundtrips-stock-future.transit-json")

(ds->transit-json-file
 report
 "src/dev/demodata/report-stock-future.transit-json")









