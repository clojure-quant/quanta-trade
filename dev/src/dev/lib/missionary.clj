(ns dev.lib.missionary
  (:require
   [tick.core :as t]
   [missionary.core :as m]))

(defn mix
  "Return a flow which is mixed by flows"
  ; will generate (count flows) processes, 
  ; so each mixed flow has its own process
  [& flows]
  (m/ap (m/?> (m/?> (count flows) (m/seed flows)))))

(defn print-task [f]
  (m/reduce (fn [r v]
              (println "v: " v)) nil f))

(def scheduler
  (m/ap
   (loop [dt (t/now)]
     (m/? (m/sleep 5000))
     (m/amb
      dt
      (recur (t/now))))))

(def a (m/seed [1 2 3]))
(def b (m/seed [:a :b :c :d :e :f]))
(def c (m/seed [:z (reduced :y) :x :w :v]))
(def m (mix a b c))


(m/? (print-task a))
(m/? (print-task b))
(m/? (print-task c))
(m/? (print-task m))
     
      
 (m/? (print-task (m/eduction
                 (take 3)
                  scheduler)))

(def print-inf (print-task scheduler))

(def x (m/mbx))


((m/race print-inf x)
 #(println "success: " %)
 #(println "err: " %))

(x :a)





