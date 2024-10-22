(ns quanta.trade.exit-test
  (:require
   [clojure.test :refer :all]
   [quanta.trade.exit-helper :refer [long-exit-prices]]))

;; see dev.exit.tests which contains identical tests for use in the repl

(deftest exit-target-prct-nogap
  ; this tests tests on one hand the entry-exit => roundtrip generation
  ; on the other hand it tests a special case where there are no wins.
  (is (= (long-exit-prices
          {:offset {; offsets create from a price a bar by adding each offset
                    :open -0.1
                    :high 0.2
                    :low -0.2
                    :close 0.0}
           :exit  [{:type :profit-prct :prct 1.0}]
           :price  [100.0 80.0 100.9 100.9 101.9 80.5]})
         [nil nil [:profit-prct 101.0] [:profit-prct 101.0] [:profit-prct 101.80000000000001] nil])))

(deftest exit-target-prct-gap
  ; this tests tests on one hand the entry-exit => roundtrip generation
  ; on the other hand it tests a special case where there are no wins.
  (is (= (long-exit-prices
          {:offset {; offsets create from a price a bar by adding each offset
                    :open -0.1
                    :high 0.2
                    :low -0.2
                    :close 0.0}
           :exit  [{:type :profit-prct :prct 1.0}]
           :price  [100.0 80.0 120.9 100.9 101.9 80.5]})
         [nil nil [:profit-prct 120.80000000000001] [:profit-prct 101.0] [:profit-prct 101.80000000000001] nil])))



(deftest exit-trailing-profit-level
  ; this tests tests on one hand the entry-exit => roundtrip generation
  ; on the other hand it tests a special case where there are no wins.
  (is (= (long-exit-prices
         {:offset {:open -0.1
                   :high 0.2
                   :low -0.2
                   :close 0.0}
          :exit [{:type :trailing-profit-level :col-long :target :col-short :target}]
          :price [100.0 80.0 100.5 100.9 101.9
                  100.1 100.2 100.3]
          :col {:atr [1.0 0.8 1.5 1.9 1.9
                      1.1 1.2 1.3]
                :target [101.5 105.0 110.0 101.7 101.9
                         101.10 101.20 101.30]}})
         [nil nil nil nil [:trailing-profit-level 101.7] nil nil nil])))




