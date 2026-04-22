(ns quanta.trade.dali.format
  (:require
   [tick.core :as t]
   [tick.helper :as th]
   [goog.string :as gstring]
   [goog.string.format]
   [goog.i18n.NumberFormat.Format])
  (:import
   (goog.i18n NumberFormat)
   (goog.i18n.NumberFormat Format)))

(defn str-format
  "Formats a string using goog.string.format.
   e.g: (format \"Cost: %.2f\" 10.0234)"
  [fmt & args]
  (apply gstring/format fmt args))

(defn nr-format [f nr]
  ;(println "fmt-nodigits nr: " nr)
  ;;(js/isNaN nr) 
  ;(= nr ##NaN) 
  ;(to-fixed nr 1)
  (if (nil? nr)
    "-"
    (if (string? nr)
      nr
      (str-format f nr))))

(defn prct [nr]
  (if (and nr (number? nr))
    (.toLocaleString nr js/undefined (clj->js {:style "percent"
                                               :minimumFractionDigits 2}))
    "-"))

(defn nr-format-0-digits [nr]
  (nr-format "%.0f" nr))

(defn nr-format-auto [nr]
  (let [nr-abs (.abs js/Math nr)]
    (nr-format (cond
                 (> nr-abs 10000) "%.0f"
                 (> nr-abs 1000) "%.1f"
                 (> nr-abs 100) "%.2f"
                 (> nr-abs 10) "%.3f"
                 (> nr-abs 1) "%.4f"
                 :else "%.5f")
               nr)))

;; date

(defn dt-format-raw [fmt dt]
  (let [zdt (-> dt
                (t/in "UTC"))]
    (t/format (t/formatter fmt) zdt)))

(defn dt-format [f dt]
  (if (nil? dt)
    ""
    (if (string? dt)
      dt
      (dt-format-raw f dt))))

(defn dt-yyyymmdd [dt]
  (dt-format "yyyy-MM-dd" dt))

(defn dt-yyyymmdd-hhmm [dt]
  (dt-format "yyyy-MM-dd HH:mm" dt))

;; bool

(defn format-bool [b]
  (if b "t" "f"))