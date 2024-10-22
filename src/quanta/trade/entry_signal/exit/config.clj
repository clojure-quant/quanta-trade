(ns quanta.trade.entry-signal.exit.config)

(defmulti exit-rule
  (fn [{:keys [type] :as opts}]
    type))