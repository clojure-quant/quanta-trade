(ns quanta.trade.report.roundtrip.performance
  (:require
   [tech.v3.datatype :as dtype]
   [tech.v3.datatype.functional :as dfn]
   [tablecloth.api :as tc]
   [ta.indicator.drawdown :refer [drawdowns-from-value xf-trailing-high]]
   [quanta.trade.report.roundtrip.return :refer [sign-switch]]))

(defn- adjust [val-vec side-vec]
  (dtype/emap sign-switch :float64 side-vec val-vec))

(defn maximum-equity [equity]
  (into [] xf-trailing-high equity))

(defn add-performance [{:keys [fee equity-initial]} roundtrip-ds]
  (let [roundtrip-ds (tc/order-by roundtrip-ds [:exit-date] [:asc])
        {:keys [side qty entry-price exit-price exit-idx entry-idx]} roundtrip-ds
        ;; pl/equity
        ; _ (println "pl/equity")
        entry-volume (dfn/* qty entry-price)
        exit-volume (dfn/* qty exit-price)
        trading-volume (dfn/+ exit-volume entry-volume)
        pl-gross (adjust (dfn/- exit-volume entry-volume) side)
        fee-m (* fee 0.01)
        pl-fee (dfn/+ (dfn/* entry-volume fee-m)
                      (dfn/* entry-volume fee-m))
        pl (dfn/- pl-gross pl-fee)
        equity  (dfn/+ (dfn/cumsum pl) equity-initial)
        equity-max (maximum-equity equity)
        ;; drawdown
        drawdown (drawdowns-from-value equity)
        drawdown-prct (dfn/* (dfn// drawdown equity-max) 100.0)
        ;; percent and log
        ;_ (println "prct/log")
        pl-points (adjust (dfn/- exit-price entry-price) side)
        pl-prct (-> 100.0 (dfn/* pl) (dfn// entry-volume))
        pl-log (adjust (dfn/- (dfn/log10 exit-price) (dfn/log10 entry-price)) side)
        cum-points  (dfn/cumsum pl-points)
        cum-log  (dfn/cumsum pl-log)
        cum-prct  (dfn/cumsum pl-prct)]
    (tc/add-columns roundtrip-ds
                    {;; volume
                     :volume-entry entry-volume
                     :volume-exit exit-volume
                     :volume-trading trading-volume
                     ;; pl/equity
                     :pl-gross pl-gross
                     :fee pl-fee
                     :pl pl
                     :win? (dfn/> pl 0.0)
                     :equity equity
                     ;; drawdown
                     :drawdown drawdown
                     :drawdown-prct drawdown-prct
                     :equity-max equity-max
                     ;; bars
                     :bars (if (and entry-idx exit-idx)
                             (dfn/- exit-idx entry-idx)
                             0)
                     ;; percent and log
                     :pl-points pl-points
                     :pl-prct pl-prct
                     :pl-log pl-log
                     :cum-points cum-points
                     :cum-log cum-log
                     :cum-prct cum-prct})))

(comment

  (require '[tick.core :as t])

  (def dt (t/instant))
  dt

  (def ds
    (tc/dataset {:asset "BTC"
                 :side [:long :short :long :short]
                 :qty 10000.0
                 :entry-idx [1 2 3 4]
                 :exit-idx [2 3 4 5]
                 :entry-date [dt dt dt dt]
                 :exit-date [dt dt dt dt]
                 :entry-price [1.0 2.0 3.0 4.0]
                 :exit-price [1.1 2.1 3.1 3.8]}))

  (require '[quanta.trade.report.roundtrip.validation :refer [validate-roundtrips-ds]])

  (validate-roundtrips-ds ds)

  (def portfolio {:fee 0.2 ; per trade in percent
                  :equity-initial 10000.0})

  (-> (add-performance portfolio ds)
      (tc/info))
    ;; => _unnamed: descriptive-stats [25 12]:
    ;;    
    ;;    |      :col-name |       :datatype | :n-valid | :n-missing |                     :min |                    :mean | :mode |                     :max | :standard-deviation |       :skew |                      :first |                       :last |
    ;;    |----------------|-----------------|---------:|-----------:|--------------------------|--------------------------|-------|--------------------------|--------------------:|------------:|-----------------------------|-----------------------------|
    ;;    |     :entry-idx |          :int64 |        4 |          0 |                    1.000 |                    2.500 |       |                    4.000 |          1.29099445 |  0.00000000 |                           1 |                           4 |
    ;;    |    :entry-date | :packed-instant |        4 |          0 | 2024-10-14T15:18:21.934Z | 2024-10-14T15:18:21.934Z |       | 2024-10-14T15:18:21.934Z |          0.00000000 |             | 2024-10-14T15:18:21.934754Z | 2024-10-14T15:18:21.934754Z |
    ;;    |   :entry-price |        :float64 |        4 |          0 |                    1.000 |                    2.500 |       |                    4.000 |          1.29099445 |  0.00000000 |                       1.000 |                       4.000 |
    ;;    |      :exit-idx |          :int64 |        4 |          0 |                    2.000 |                    3.500 |       |                    5.000 |          1.29099445 |  0.00000000 |                           2 |                           5 |
    ;;    |          :side |        :keyword |        4 |          0 |                          |                          | :long |                          |                     |             |                       :long |                      :short |
    ;;    |           :qty |        :float64 |        4 |          0 |                1.000E+04 |                1.000E+04 |       |                1.000E+04 |          0.00000000 |             |                   1.000E+04 |                   1.000E+04 |
    ;;    |    :exit-price |        :float64 |        4 |          0 |                    1.100 |                    2.525 |       |                    3.800 |          1.17862915 | -0.28812463 |                       1.100 |                       3.800 |
    ;;    |         :asset |         :string |        4 |          0 |                          |                          |   BTC |                          |                     |             |                         BTC |                         BTC |
    ;;    |     :exit-date | :packed-instant |        4 |          0 | 2024-10-14T15:18:21.934Z | 2024-10-14T15:18:21.934Z |       | 2024-10-14T15:18:21.934Z |          0.00000000 |             | 2024-10-14T15:18:21.934754Z | 2024-10-14T15:18:21.934754Z |
    ;;    |           :fee |        :float64 |        4 |          0 |                    40.00 |                    100.0 |       |                    160.0 |         51.63977795 |  0.00000000 |                       40.00 |                       160.0 |
    ;;    |       :cum-log |        :float64 |        4 |          0 |                  0.02020 |                  0.03819 |       |                  0.05672 |          0.01517870 |  0.09987864 |                     0.04139 |                     0.05672 |
    ;;    |   :volume-exit |        :float64 |        4 |          0 |                1.100E+04 |                2.525E+04 |       |                3.800E+04 |      11786.29147216 | -0.28812463 |                   1.100E+04 |                   3.800E+04 |
    ;;    |        :equity |        :float64 |        4 |          0 |                     9880 |                1.105E+04 |       |                1.260E+04 |       1134.84213293 |  0.95660058 |                   1.096E+04 |                   1.260E+04 |
    ;;    |          :bars |          :int64 |        4 |          0 |                    1.000 |                    1.000 |       |                    1.000 |          0.00000000 |             |                           1 |                           1 |
    ;;    |          :win? |        :boolean |        4 |          0 |                          |                          |  true |                          |                     |             |                        true |                        true |
    ;;    |  :volume-entry |        :float64 |        4 |          0 |                1.000E+04 |                2.500E+04 |       |                4.000E+04 |      12909.94448736 |  0.00000000 |                   1.000E+04 |                   4.000E+04 |
    ;;    |      :cum-prct |        :float64 |        4 |          0 |                4.200E+04 |                8.167E+04 |       |                1.173E+05 |      32443.68340051 | -0.29673028 |                   9.600E+04 |                   1.173E+05 |
    ;;    |        :pl-log |        :float64 |        4 |          0 |                 -0.02119 |                  0.01418 |       |                  0.04139 |          0.02618570 | -0.87492602 |                     0.04139 |                     0.02228 |
    ;;    |            :pl |        :float64 |        4 |          0 |                    -1080 |                    650.0 |       |                     1840 |       1232.61240191 | -1.22835461 |                       960.0 |                        1840 |
    ;;    |      :pl-gross |        :float64 |        4 |          0 |                    -1000 |                    750.0 |       |                     2000 |       1258.30573921 | -1.12933811 |                        1000 |                        2000 |
    ;;    |       :pl-prct |        :float64 |        4 |          0 |               -5.400E+04 |                2.933E+04 |       |                9.600E+04 |      62360.95644623 | -0.76360355 |                   9.600E+04 |                   4.600E+04 |
    ;;    |     :pl-points |        :float64 |        4 |          0 |                  -0.1000 |                  0.07500 |       |                   0.2000 |          0.12583057 | -1.12933811 |                      0.1000 |                      0.2000 |
    ;;    |    :cum-points |        :float64 |        4 |          0 |                    0.000 |                   0.1250 |       |                   0.3000 |          0.12583057 |  1.12933811 |                      0.1000 |                      0.3000 |
    ;;    |      :drawdown |        :float64 |        4 |          0 |                    0.000 |                    320.0 |       |                     1080 |        515.36394907 |  1.81039448 |                       0.000 |                       0.000 |
    ;;    | :drawdown-prct |        :float64 |        4 |          0 |                    0.000 |                    3.197 |       |                    10.93 |          5.22972327 |  1.84000667 |                       0.000 |                       0.000 |

  (-> (add-performance portfolio ds)
      (tc/select-columns [:exit-date :side :fee :pl :equity :drawdown :drawdown-prct]))
  ;; => _unnamed [4 7]:
  ;;    
  ;;    |                  :exit-date |  :side |  :fee |     :pl | :equity | :drawdown | :drawdown-prct |
  ;;    |-----------------------------|--------|------:|--------:|--------:|----------:|---------------:|
  ;;    | 2024-10-14T15:18:21.934754Z |  :long |  40.0 |   960.0 | 10960.0 |       0.0 |     0.00000000 |
  ;;    | 2024-10-14T15:18:21.934754Z | :short |  80.0 | -1080.0 |  9880.0 |    1080.0 |    10.93117409 |
  ;;    | 2024-10-14T15:18:21.934754Z |  :long | 120.0 |   880.0 | 10760.0 |     200.0 |     1.85873606 |
  ;;    | 2024-10-14T15:18:21.934754Z | :short | 160.0 |  1840.0 | 12600.0 |       0.0 |     0.00000000 |

  (Math/pow 10 2.8132)

  (dfn/cumsum [1 2 3])
  ; we want to operate on log-10. With them *10 = 1
  (->>  (Math/log10 13)
        (Math/pow 10))

  (defn log10 [a]
    (Math/log10 a))

  (->>  [0.01 0.1 1 10 100 100]
        (map log10))
  ; negative logs mean we have lost money
  ; so log-pl negative=loss positive=profit

  (let [lo (log10 5601.5)
        lc (log10 57159.0)
        d (- lc lo)]
    (Math/pow d 10))
     ; 1.09    1=*10
     ;          0.09 = + a little bit

  (let [p 120
        l 40
        plog (Math/log10 p)
        llog (Math/log10 l)
        diff (- plog llog)]
    [plog llog diff (Math/pow 10 diff)])

  (- (Math/log10 101) (Math/log10 100)) ; 1% 0.004

  (- (Math/log10 120) (Math/log10 100)) ; 20% 0.08
  (- (Math/log10 1200) (Math/log10 1000)) ; 20% 0.08
  (- (Math/log10 1000) (Math/log10 2000)) ; -0.3
  (- (Math/log10 2000) (Math/log10 1000)) ; +0.3

;   
  )