(ns quanta.trade.backtest.from-entry
  (:require
   [tech.v3.dataset :as tds]
   [tablecloth.api :as tc]
   [missionary.core :as m]))

(defn row-at [ds idx]
  (let [ds (-> ds
               (tc/select-rows idx)
               (tds/mapseq-reader ds))
        v (into [] ds)]
     (last v)))

(defn from-algo-cell [algo-cell]
  (m/stream
   (m/ap (when-let [ds (m/?> algo-cell)]
           (let [n (tc/row-count ds)
                 r (range n)
                 ds-idx (tc/add-column ds :idx r)]
             (loop [idx 0]
               (let [row (row-at ds-idx idx)
                     output {:data row}
                     entry (:entry row)
                     output (case entry 
                               :long (assoc output :entry-signal entry)
                               :short (assoc output :entry-signal entry)
                               output)]
                 (if (< (inc idx) n)
                   (m/amb
                      output
                      (recur (inc idx)))
                   (m/amb (assoc output :shutdown true)
                          (reduced {:shutdown :now})
                          )))))))))


(defn from-algo-ds [ds]
  (let [algo-cell (m/seed [ds])]
    (from-algo-cell algo-cell)))
  
