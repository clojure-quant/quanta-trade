(ns dev.report.roundtrip-metrics
  (:require
   [tablecloth.api :as tc]
   [quanta.trade.report.roundtrip.metrics :as m]))

(def ds
  (tc/dataset
   {:side [:long :short :long :short]
    :pl [-0.30103000  0.17609126  -0.12493874 0.09691001]
    :win? [true false true false]
    :bars [1 1 1 1]}))

(m/calc-roundtrip-stats ds [:win?])
;; => _unnamed [2 7]:
;;    
;;    | :win? | :bars | :trades | :trading-volume |         :pl |    :pl-mean |  :pl-median |
;;    |-------|------:|--------:|----------------:|------------:|------------:|------------:|
;;    |  true |   2.0 |       2 |             0.0 | -0.42596874 | -0.21298437 | -0.12493874 |
;;    | false |   2.0 |       2 |             0.0 |  0.27300127 |  0.13650064 |  0.17609126 |

(m/win-loss-stats ds)
;; => _unnamed [2 7]:
;;    
;;    | :win? | :bars | :trades | :trading-volume |         :pl |    :pl-mean |  :pl-median |
;;    |-------|------:|--------:|----------------:|------------:|------------:|------------:|
;;    |  true |   2.0 |       2 |             0.0 | -0.42596874 | -0.21298437 | -0.12493874 |
;;    | false |   2.0 |       2 |             0.0 |  0.27300127 |  0.13650064 |  0.17609126 |

(m/side-stats ds)
;; => _unnamed [2 7]:
;;    
;;    |  :side | :bars | :trades | :trading-volume |         :pl |    :pl-mean |  :pl-median |
;;    |--------|------:|--------:|----------------:|------------:|------------:|------------:|
;;    |  :long |   2.0 |       2 |             0.0 | -0.42596874 | -0.21298437 | -0.12493874 |
;;    | :short |   2.0 |       2 |             0.0 |  0.27300127 |  0.13650064 |  0.17609126 |

(def side-s (m/side-stats ds))

side-s

(m/get-group-of side-s :side :long)
;; => {:side :long, :bars 2.0, :trades 2, :trading-volume 0.0, :pl -0.42596874, 
;;     :pl-mean -0.21298437, :pl-median -0.12493874}

(m/get-group-of side-s :side :short)
(m/get-group-of side-s :side :parrot)

(m/calc-roundtrip-metrics ds)
;; => {:pf 1.5603177963238046,
;;     :win
;;     {:trades 2,
;;      :bars 2.0,
;;      :trading-volume 0.0,
;;      :pl -0.42596874,
;;      :pl-mean -0.21298437,
;;      :pl-median -0.12493874,
;;      :trade-prct 50.0,
;;      :bar-avg 1.0},
;;     :loss
;;     {:trades 2,
;;      :bars 2.0,
;;      :trading-volume 0.0,
;;      :pl 0.27300127,
;;      :pl-mean 0.136500635,
;;      :pl-median 0.17609126,
;;      :trade-prct 50.0,
;;      :bar-avg 1.0},
;;     :all
;;     {:trades 4,
;;      :bars 4.0,
;;      :trading-volume 0.0,
;;      :pl -0.15296747,
;;      :pl-mean -0.038241867500000006,
;;      :pl-median 0.09691001,
;;      :trade-prct 100.0,
;;      :bar-avg 1.0}}


