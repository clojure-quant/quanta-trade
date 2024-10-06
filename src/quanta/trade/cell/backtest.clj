(ns quanta.trade.cell.backtest
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
               (let [ds-until-idx (tc/select-rows ds-idx (range 0 (inc idx)))]
                 (if (< (inc idx) n)
                   (m/amb
                     {:idx idx
                      :row (row-at ds-idx idx)
                      :ds ds-until-idx}
                      (recur (inc idx)))
                   (m/amb
                    {:idx idx
                     :row (row-at ds-idx idx)
                     :ds ds-until-idx})))))))))



