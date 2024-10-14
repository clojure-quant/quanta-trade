(ns dev.trade-entry-exit
  (:require
   [tick.core :as t]
   [tablecloth.api :as tc]
   [ta.trade.backtest.from-entry :refer [entry-signal->roundtrips]]
   [quanta.trade.report.roundtrip :refer [roundtrip-stats]]))

(def ds (tc/dataset {:date (repeatedly 6 #(t/instant))
                     :close [100.0 104.0 106.0 103.0 102.0 108.0]
                     :high [100.0 104.0 106.0 103.0 102.0 108.0]
                     :low [100.0 104.0 106.0 103.0 102.0 108.0]
                     :entry [:long :nil nil :short :nil :nil]}))

ds

(def rts (-> (entry-signal->roundtrips {:asset "QQQ"
                                        :entry [:fixed-qty 3.1]
                                        :exit [:time 2
                                               :loss-percent 2.5
                                               :profit-percent 5.0]}
                                       ds)
             :roundtrips))

rts
;; => _unnamed [2 11]:
;;    
;;    | :entry-idx |                 :entry-date | :entry-price | :exit-idx | :id |  :side | :qty | :exit-price |      :exit-rule | :asset |                  :exit-date |
;;    |-----------:|-----------------------------|-------------:|----------:|----:|--------|-----:|------------:|-----------------|--------|-----------------------------|
;;    |          0 | 2024-10-14T16:24:07.291510Z |        100.0 |         2 |   1 |  :long |  3.1 |     105.000 | :profit-percent |    QQQ | 2024-10-14T16:24:07.291666Z |
;;    |          3 | 2024-10-14T16:24:07.291679Z |        103.0 |         5 |   2 | :short |  3.1 |     105.575 |   :loss-percent |    QQQ | 2024-10-14T16:24:07.291685Z |

(def portfolio {:fee 0.2 ; per trade in percent
                :equity-initial 10000.0})

(def stats
  (roundtrip-stats portfolio rts))

stats

(-> (:roundtrip-ds stats)
    (tc/select-columns [:exit-date :side :volume-entry :fee :pl :equity :drawdown :drawdown-prct]))
;; => _unnamed [2 8]:
;;    
;;    |                  :exit-date |  :side | :volume-entry |   :fee |     :pl |    :equity | :drawdown | :drawdown-prct |
;;    |-----------------------------|--------|--------------:|-------:|--------:|-----------:|----------:|---------------:|
;;    | 2024-10-14T16:24:07.291666Z |  :long |         310.0 | 1.2400 | 14.2600 | 10014.2600 |    0.0000 |     0.00000000 |
;;    | 2024-10-14T16:24:07.291685Z | :short |         319.3 | 1.2772 | -9.2597 | 10005.0003 |    9.2597 |     0.09255072 |

(:metrics stats)
;; => {:nav
;;     {:equity-final 10005.0003,
;;      :cum-pl 5.00030000000004,
;;      :fee-total 2.5172,
;;      :max-drawdown 9.259700000000521,
;;      :max-drawdown-prct 0.09255072186255228},
;;     :roundtrip
;;     {:pf 1.0,
;;      :win {:trades 1, :bars 2.0, :pl 14.26, :pl-mean 14.26, :trade-prct 50.0, :bar-avg 2.0},
;;      :loss {:trades 1, :bars 2.0, :pl -9.25969999999996, :pl-mean -9.25969999999996, :trade-prct 50.0, :bar-avg 2.0},
;;      :all {:trades 2, :bars 4.0, :pl 5.00030000000004, :pl-mean nil, :trade-prct 100.0, :bar-avg 2.0}}}

(def alex-ds (tc/dataset {:asset ["BTC" "BTC" "BTC"]
                          :close [1.0 2.0 3.0]
                          :low [1.0 2.0 3.0]
                          :high [1.0 2.0 3.0]
                          :date [(t/instant "1999-02-01T20:00:00Z")
                                 (t/instant "2000-02-01T20:00:00Z")
                                 (t/instant "2001-02-01T20:00:00Z")]
                          :entry-bool [false false true]
                          :entry [:flat :short :flat]
                          :bars-above-b1h 51
                          :d [1.0 Double/NaN nil]}))

(->>  alex-ds
      (entry-signal->roundtrips {:asset "BTC"
                                 :entry [:fixed-amount 100000]
                                 :exit [:time 5
                                        :loss-percent 4.0
                                        :profit-percent 5.0]})
      :roundtrips
      (roundtrip-stats portfolio))



