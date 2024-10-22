(ns quanta.trade.exit-helper
  (:require
   [tick.core :as t]
   [quanta.trade.entry-signal.rule :as rule]))

(defn long-exits
  "creates an entrysignal manager 
   with a long positon at price 100.0 entry-idx 0
   returns state that is used in exit?"
  [exit]
  (let [m (rule/create-entrysignal-manager
           {:asset "QQQ"
            :entry {:type :fixed-qty :fixed-qty 1.0}
            :exit exit})
        dt (t/instant "2020-01-01T00:00:00Z")]
    (rule/on-position-open m
                           {:id 5
                            :asset "QQQ"
                            :side :long
                            :entry-price 100.0
                            :entry-idx 0
                            :entry-date dt
                            :qty 100.0})
    {:m m
     :exit-idx (atom 0)
     :exit-date (atom dt)}))

(defn inc-day [dt]
  (-> dt
      (t/>> (t/new-duration 1 :days))))

(defn extract [rt]
  (when rt
    (let [{:keys [exit-reason exit-price]} rt]
      [exit-reason exit-price])))

(defn convert-to-vectors [data]
  (let [cols (keys data)]
    (mapv (fn [index]
            (zipmap
             cols
             (map #(nth (get data %) index) cols)))
          (range (count (first (vals data)))))))

(defn exit?
  "checks for exits at price, 
   automatically increases idx and date"
  [{:keys [m exit-idx exit-date]}
   {:keys [open high low close] :as offset}
   price row]
  (swap! exit-idx inc)
  (swap! exit-date inc-day)
  (let [bar {:open (+ price open)
             :high (+ price high)
             :low (+ price low)
             :close (+ price close)
             :date @exit-date
             :idx @exit-idx}
        row (merge bar row)]
    ;(println "check-exit bar: " row)
    (-> (rule/check-exit m row)
        first
        extract)))

(defn long-exit-prices [{:keys [exit price offset col]}]
  (let [m (long-exits exit)
        rows (when col (convert-to-vectors col))]
    (into []
          (if rows
            (map (fn [p r]
                   (exit? m offset p r)) price rows)
            (map (fn [p]
                   (exit? m offset p {})) price)))))

(comment
  (inc-day (t/instant))
  (def exit
    [{:type :profit-prct :prct 1.0}
     {:type :profit-prct :prct 5.0}])

  (def m (long-exits exit))
  (exit? m 100.0)
  (exit? m 100.5)
  (exit? m 100.9)
  (exit? m 101.9)

  (long-exit-prices exit [100.0 100.5 100.9 101.9])

  (def col
    {:atr [1.0 0.8 1.5 1.9 1.9
           1.1 1.2 1.3]
     :atr2 [5.0 3.8 2.5 5.9 2.9
            3.1 6.2 5.3]})

  (convert-to-vectors col)
  (convert-to-vectors nil)

;  
  )