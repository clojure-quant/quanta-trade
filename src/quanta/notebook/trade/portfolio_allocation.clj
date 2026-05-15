(ns quanta.notebook.trade.portfolio-allocation
  (:require
   [tablecloth.api :as tc]
   [tick.core :as t]
   [quanta.trade.portfolio-allocation :as pa]
   [quanta.calendar.ds.window :refer [create-idx-links]]))

(def return-ds (tc/dataset {:date [(t/date "2020-01-01") (t/date "2020-01-02") (t/date "2020-01-03") (t/date "2020-01-04")]
                            "A" [0.10 -0.10 0.15 0.20]
                            "B" [0.00 0.20 0.10 0.15]}))

return-ds

(def allocation-ds (tc/dataset {:date [(t/date "2020-01-01") (t/date "2020-01-02") (t/date "2020-01-03") (t/date "2020-01-04")]
                                "A" [0.5 1.0 0.0 0.0]
                                "B" [0.5 0.0 1.0 0.0]}))

allocation-ds

(pa/portfolio-return return-ds allocation-ds)

(pa/portfolio-cumulative-returns (pa/portfolio-return return-ds allocation-ds))

(def allocation2-ds (tc/dataset {:date [(t/date "2020-01-02") (t/date "2020-01-03")]
                                 "A" [0.5  0.0]
                                 "B" [0.5  1.0]}))

(pa/align-allocation-ds return-ds allocation2-ds)