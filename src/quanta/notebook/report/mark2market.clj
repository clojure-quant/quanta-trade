(ns quanta.notebook.trade.report.mark2market
  (:require
   [tick.core :as t] 
   [ta.trade.roundtrip.nav.mark2market :refer [portfolio]]
   [ta.viz.trade.m2m.core :refer [m2m-chart]]
   [ta.db.bars.protocol :as b]
   [modular.system :refer [system]]
   ))

(def bardb (:bar-db system))

(def rts [{:asset "ETHUSDT"
           :side :long
           :qty 100.0
           :entry-date (t/instant "2023-03-09T23:59:59Z")
           :entry-price 1400.78
           :exit-date (t/instant "2023-03-16T23:59:59Z")
           :exit-price 1600.92}])

(-> (portfolio bardb rts {:calendar [:crypto :d]
                          :import :bybit})
    (m2m-chart)
 ;pr-str
    )

; {:open# 0, :long$ 0.0, :short$ 0.0, :net$ 0.0, :pl-u 0.0, 
; :pl-r 0.0, :date #inst \"2023-03-06T23:59:59.000000000-00:00\",
; :pl-r-cum 0.0, :pl-cum 0.0}


