(ns dev.lib.dataset
  (:require 
    [tablecloth.api :as tc]
   [quanta.trade.backtest.store :refer [save-ds-transit-safe]]
   )
  
  )


(def ds (tc/dataset {:a [1 2 3]
                     :b [:a :b :C]
                     }))

ds

(save-ds-transit-safe ds ".data/test.transit-json")