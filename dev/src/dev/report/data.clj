(ns report.data
  (:require
   [tick.core :as t]
   [clojure.edn :as edn]))

(defn clean [row]
  (-> row
      (update :entry-date t/date)
      (update :exit-date t/date)))

(defn load-roundtrips []
  (->> (slurp "roundtrips.edn")
       (edn/read-string)
       (map clean)))


(comment 
  (t/date "2023-01-10")
  (load-roundtrips)
  
  
;  
  )









