(ns dev.algo-bollinger-bruteforce
  (:require
   [tick.core :as t]
   [clojure.pprint :refer [print-table]]
   [quanta.dag.core :as dag]
   [quanta.algo.core :as algo]
   [quanta.algo.options :refer [make-variations create-algo-variations variation-keys]]
   [quanta.trade.bruteforce :refer [bruteforce] :as bf]
   [quanta.market.barimport.bybit.import :as bybit]
   [dev.algo-bollinger :refer [bollinger-algo]]))

;; ENV

(def bar-db (bybit/create-import-bybit))

(def env {:bar-db bar-db})

(def dag-env
  {:log-dir ".data/"
   :env env})

(def dt (t/instant))

(def variations
  {[:* :asset] ["BTCUSDT" "ETHUSDT"]
   [:algo :atr-n] [20 50]})

(variation-keys variations)
;; => ([0 :asset] [2 :day :atr-n])

(make-variations variations)
;; => ({[0 :asset] "BTCUSDT", [2 :atr-n] 20}
;;     {[0 :asset] "BTCUSDT", [2 :atr-n] 50}
;;     {[0 :asset] "ETHUSDT", [2 :atr-n] 20}
;;     {[0 :asset] "ETHUSDT", [2 :atr-n] 50})

(create-algo-variations bollinger-algo variations)

(->> (create-algo-variations bollinger-algo variations)
     (map #(bf/summarize % variations))
     print-table)
; | [0 :asset] | [2 :atr-n] |
; |------------+------------|
; |    BTCUSDT |         20 |
; |    BTCUSDT |         50 |
; |    ETHUSDT |         20 |
; |    ETHUSDT |         50 |

(defn get-pf [r]
  (-> r :metrics :roundtrip :pf))

(defn show-fn [r]
  (-> r :metrics :roundtrip (select-keys [:trades])))

(-> (bruteforce dag-env
                {:algo bollinger-algo
                 :cell-id :backtest
                 :options {[:bars :trailing-n] 500}
                 :variations variations
                 :target-fn get-pf
                 :show-fn show-fn
                 :dt dt
                 :label "brute1"})

    :ok
    print-table)

; | [0 :asset] | [2 :day :atr-n] |            :target | :trades |    :id |
; |------------+-----------------+--------------------+---------+--------|
; |    BTCUSDT |              50 | 0.5196291340803781 |     128 | ckdJDj |
; |    BTCUSDT |              20 | 0.5196291340803781 |     128 | qQsof9 |
; |    ETHUSDT |              50 |  0.471267441188845 |     134 | JNSsjP |
; |    ETHUSDT |              20 |  0.471267441188845 |     134 | BWR3q4 |

(bf/show-available ".data/bruteforce/")
;; => ("brute1")

(bf/load-label ".data/bruteforce/" "brute1")
