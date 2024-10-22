(ns quanta.trade.entry-signal.exit.config.multiple
  (:require
   [quanta.trade.entry-signal.exit.config :refer [exit-rule]]
   [quanta.trade.entry-signal.exit.config.profit] ; side effects 
   [quanta.trade.entry-signal.exit.config.loss] ; side effects
   [quanta.trade.entry-signal.exit.config.time] ; side effects 
   [quanta.trade.entry-signal.exit.position.multiple] ; side effects 
   )
  (:import
   [quanta.trade.entry_signal.exit.position.multiple MultipleRules]))

(defn setup-exit-rules [exit]
  (map exit-rule exit))

(defn create-exit-manager-for-position
  "create a exit-fn for one position. 
   this gets run on each bar, while the positon is open"
  [exit-rules position]
  ;(println "creating exit-rules for position: " position)
  ;(println "exit rules:  " rules)
  (let [position-rules (map #(% position) exit-rules)]
    (MultipleRules. position position-rules)))