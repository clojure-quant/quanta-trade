(ns quanta.trade.entry-signal.exit.position.multiple
  (:require
   [quanta.trade.entry-signal.exit.position :refer [IExit check-exit priority get-level]]))

(defrecord MultipleRules [position rules]
  IExit
  (check-exit [_ {:keys [high low] :as row}]
    (->> rules
         (map #(check-exit % row))
         (remove nil?)
         first))
  (get-level [_]
    (let [rules-loss (filter #(= 1 (priority %)) rules)
          rules-profit (filter #(= 2 (priority %)) rules)
          level-loss (->> (map #(get-level %) rules-loss)
                          (into []))

          level-profit  (->> (map #(get-level %) rules-profit)
                             (into []))
          result {:side (:side position)
                  :profit level-profit
                  :loss level-loss}]
       ;(println "multiple-rules get-level: " result)  
      result)))


