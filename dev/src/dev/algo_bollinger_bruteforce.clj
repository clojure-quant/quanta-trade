(ns dev.algo-bollinger-bruteforce
  (:require
   [tick.core :as t]
   [clojure.pprint :refer [print-table]]
   [quanta.dag.core :as dag]
   [quanta.algo.env.bars]
   [quanta.algo.core :as algo]
   [quanta.algo.options :refer [make-variations create-algo-variations]]
   [quanta.trade.bruteforce :refer [bruteforce] :as bf]
   [ta.import.provider.bybit.ds :as bybit]
   [dev.algo-bollinger :refer [bollinger-algo]]))

;; ENV

(def bar-db (bybit/create-import-bybit))
(def env {#'quanta.algo.env.bars/*bar-db* bar-db})

(def dag-env
  {:log-dir ".data/"
   :env env})

(def dt (t/instant))

(def variations
  [[0 :asset] ["BTCUSDT" "ETHUSDT"]
   [2 :day :atr-n] [20 50]])

(bf/variation-keys variations)
;; => ([0 :asset] [2 :day :atr-n])

(make-variations variations)
;; => ({[0 :asset] "BTCUSDT", [2 :day :atr-n] 20}
;;     {[0 :asset] "BTCUSDT", [2 :day :atr-n] 50}
;;     {[0 :asset] "ETHUSDT", [2 :day :atr-n] 20}
;;     {[0 :asset] "ETHUSDT", [2 :day :atr-n] 50})

(create-algo-variations bollinger-algo variations)

(->> (create-algo-variations bollinger-algo variations)
     (map #(bf/summarize % variations))
     print-table)
; | [0 :asset] | [2 :day :atr-n] |
; |------------+-----------------|
; |    BTCUSDT |              20 |
; |    BTCUSDT |              50 |
; |    ETHUSDT |              20 |
; |    ETHUSDT |              50 |

(defn get-pf [r]
  (-> r :metrics :roundtrip :pf))

(defn show-fn [r]
  (-> r :metrics :roundtrip (select-keys [:trades])))

(-> (bruteforce dag-env
                {:algo bollinger-algo
                 :cell-id :backtest
                 :variations variations
                 :target-fn get-pf
                 :show-fn show-fn
                 :dt dt
                 :label "brute1"})
    print-table)

; | [0 :asset] | [2 :day :atr-n] |            :target | :trades |
; |------------+-----------------+--------------------+---------|
; |    BTCUSDT |              50 | 0.6040295470644278 |     126 |
; |    BTCUSDT |              20 | 0.6040295470644278 |     126 |
; |    ETHUSDT |              50 | 0.4967416107940467 |     131 |
; |    ETHUSDT |              20 | 0.4967416107940467 |     131 |

(-> ".data/bruteforce/brute1.edn"
    slurp
    read-string)


