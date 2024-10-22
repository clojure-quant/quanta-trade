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