
(ns quanta.trade.dali.cheetah-style)

(defn red-color [row]
  ;(println "red-color for: " row)
  (clj->js {:color "red"}))

(defn blue-color [row]
  ;(println "blue-color for: " row)
  (clj->js {:color "blue"}))

(defn bool-color [row]
  (let [row-clj (js->clj row)
        v (get row-clj "cross-up-c")
        color (if v "blue" "red")]
    ;(println row-clj)
    ;(println "bool color: " color " val: " v)
    (clj->js {:color color})))