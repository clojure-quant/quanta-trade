(ns quanta.trade.entry-signal.exit.position.multiple
  (:require
   [quanta.trade.entry-signal.exit.position :as p :refer [IExit check-exit priority get-level]]))

(defrecord MultipleRules [position rules]
  IExit
  (priority [_]
    0)
  (check-exit [_ {:keys [high low] :as row}]
    (->> rules
         (map #(check-exit % row))
         (remove nil?)
         first))
  (get-level [_]
    (let [rules-loss (filter #(= 1 (p/priority %)) rules)
          rules-profit (filter #(= 2 (p/priority %)) rules)
          level-loss (->> (map #(get-level %) rules-loss)
                          (into []))

          level-profit  (->> (map #(get-level %) rules-profit)
                             (into []))
          result {:side (:side position)
                  :profit level-profit
                  :loss level-loss}]
       ;(println "multiple-rules get-level: " result)  
      result)))


