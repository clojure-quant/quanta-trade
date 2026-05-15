(ns quanta.trade.portfolio-allocation
  "Align return and allocation datasets by row (:date must match); weighted returns and compounding.
  Asset columns use **string** names (e.g. \"SPY\"); :date and :portfolio are **keywords**."
  (:require
   [tick.core :as t]
   [tablecloth.api :as tc]
   [tech.v3.datatype.functional :as dfn]
   [quanta.calendar.ds.window :refer [create-idx-links]]
   [quanta.calendar.ds.nil :refer [fill-forward]]))

(defn- allocation-0-ds
  "Appends one row: :date is strictly after every date in `return-ds` and `allocation-ds` (so
  `create-idx-links` never returns its index from a date match); every other column is 0.0."
  [return-ds allocation-ds]
  (let [cols (tc/column-names allocation-ds)
        date-max (apply t/max (concat (vec (tc/column allocation-ds :date))
                                      (vec (tc/column return-ds :date))))
        sentinel (.plusDays date-max 1)
        zero-row (into {}
                       (map (fn [c] [c (if (= :date c) sentinel 0.0)]))
                       cols)]
    (tc/concat allocation-ds (tc/dataset [zero-row]))))

(defn align-allocation-ds [return-ds allocation-ds]
  (let [alloc-ext (allocation-0-ds return-ds allocation-ds)
        zero-row-idx (dec (tc/row-count alloc-ext))
        idx-vec (->> (create-idx-links return-ds alloc-ext)
                     (fill-forward)
                     (mapv #(if (some? %) % zero-row-idx)))]
    (-> alloc-ext
        (tc/select-rows idx-vec)
        (tc/add-columns {:date (:date return-ds)}))))

(defn- str-asset-col
  "Output column name for an asset (always a string; :date / :portfolio stay keywords)."
  [c]
  (cond
    (keyword? c) (name c)
    (string? c) c
    :else (str c)))

(defn- asset-columns
  "Non-:date column names in dataset order (as returned by Tablecloth)."
  [ds]
  (vec (remove #(= :date %) (tc/column-names ds))))

(defn- assert-matching-dates!
  [return-ds allocation-ds]
  (when-not (= (into [] (tc/column return-ds :date))
               (into [] (tc/column allocation-ds :date)))
    (throw (ex-info ":date columns differ between return and allocation datasets"
                    {:return-dates (into [] (tc/column return-ds :date))
                     :allocation-dates (into [] (tc/column allocation-ds :date))}))))

(defn portfolio-return
  "Row-wise return * allocation for each asset column (names from `return-ds`).
  Output uses keyword :date and **string** asset column names (keyword asset cols in the inputs are
  renamed via `name` in the result). Both datasets must have the same row count and identical :date
  column (see `assert-matching-dates!`)."
  [return-ds allocation-ds]
  (assert-matching-dates! return-ds allocation-ds)
  (let [assets (asset-columns return-ds)
        dates (tc/column return-ds :date)
        weighted
        (into (array-map :date (into [] dates))
              (map (fn [a]
                     [(str-asset-col a)
                      (dfn/* (tc/column return-ds a)
                             (tc/column allocation-ds a))]))
              assets)]
    (tc/dataset weighted)))

(defn- compound-cumulative
  [period-returns]
  (loop [rs (seq period-returns)
         acc 1.0
         out (transient [])]
    (if-not rs
      (persistent! out)
      (let [r (double (first rs))
            acc' (* acc (+ 1.0 r))]
        (recur (next rs) acc' (conj! out (dec acc')))))))

(defn portfolio-cumulative-returns
  "Takes a portfolio-return dataset (:date + one string-named column per asset, each cell a period
  weighted return). Replaces each asset column with its cumulative compounded return, and adds keyword
  :portfolio — cumulative compound return of the row sum of asset columns (period portfolio return)."
  [portfolio-return-ds]
  (let [assets (asset-columns portfolio-return-ds)
        dates (into [] (tc/column portfolio-return-ds :date))
        n (tc/row-count portfolio-return-ds)
        period-portfolio-vec
        (vec (if (seq assets)
               (apply map (fn [& xs] (double (reduce + 0.0 xs)))
                      (map #(into [] (tc/column portfolio-return-ds %)) assets))
               (repeat n 0.0)))
        portfolio-cum (compound-cumulative period-portfolio-vec)
        ordered
        (-> (array-map :date dates)
            (into (map (fn [a]
                         [(str-asset-col a)
                          (compound-cumulative (into [] (tc/column portfolio-return-ds a)))])
                       assets))
            (assoc :portfolio portfolio-cum))]
    (tc/dataset ordered)))