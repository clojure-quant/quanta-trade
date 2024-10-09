(ns dev.exit.trailing-stop
  (:require
   [quanta.trade.entry-signal.rule.exit2 :refer [check-exit]]
   [quanta.trade.entry-signal.rule.exit-config :refer [exit-rule]]))

(def configured-rule (exit-rule {:type :trailing-stop-offset
                                 :col :atr}))

(def rule (configured-rule
           {:entry-price 10000
            :side :long}
           {:close 10000
            :atr 90}))

(check-exit rule {:close 11000
                  :high 11000
                  :low 11000
                  :atr 100})

(check-exit rule {:close 10000
                  :high 10000
                  :low 10000
                  :atr 100})



