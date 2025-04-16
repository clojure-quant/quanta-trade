(ns quanta.trade.util
  (:require
   [tablecloth.api :as tc]))

(defn has-col? [ds col-kw]
  (->> ds
       tc/columns
       (map meta)
       (filter #(= col-kw (:name %)))
       first))