(ns dev.util
  (:require
   [missionary.core :as m]))

(defn start-logging [file-name flow]
  (let [print-task (m/reduce (fn [r v]
                               (let [s (with-out-str (println v))]
                                 (spit file-name s :append true))
                               nil)
                             nil flow)]
    (print-task
     #(println "flow-logger completed: " %)
     #(println "flow-logger crashed: " %))))
