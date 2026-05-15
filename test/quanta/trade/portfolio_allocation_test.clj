(ns quanta.trade.portfolio-allocation-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [tablecloth.api :as tc]
   [tick.core :as t]
   [quanta.trade.portfolio-allocation :as pa]))

(defn- col-near?
  [expected actual ^double tol]
  (and (= (count expected) (count actual))
       (every? #(< (Math/abs %) tol) (map - expected actual))))

(defn- ds-ordered
  "Dataset with keyword :date then string asset columns, in a fixed column order."
  [date-vec pairs]
  (tc/dataset
   (into (array-map :date date-vec)
         (map (fn [[sym v]]
                [(if (string? sym) sym (name sym)) v]))
         pairs)))

(deftest align-allocation-ds-leading-nil-idx-uses-zero-weights
  (testing "return dates before first allocation date: weights 0.0; later rows match allocation by date"
    ;; create-idx-links + fill-forward leaves leading nils when the bar ds starts after the calendar;
    ;; those must not pick real allocation rows — they should use a synthetic all-zero allocation row.
    (let [d1 (t/date "2020-01-01")
          d2 (t/date "2020-01-02")
          d3 (t/date "2020-01-03")
          d4 (t/date "2020-01-04")
          return-ds (ds-ordered [d1 d2 d3 d4] [[:A [0.01 0.02 0.03 0.04]] [:B [-0.01 0.0 0.01 0.02]]])
          allocation-ds (ds-ordered [d3 d4] [[:A [0.6 0.4]] [:B [0.4 0.6]]])
          aligned (pa/align-allocation-ds return-ds allocation-ds)]
      (is (= [d1 d2 d3 d4] (into [] (tc/column aligned :date))))
      (is (= [0.0 0.0 0.6 0.4] (into [] (tc/column aligned "A"))))
      (is (= [0.0 0.0 0.4 0.6] (into [] (tc/column aligned "B")))))))

(deftest portfolio-return-and-cumulative
  (testing "portfolio-return multiplies each asset return by its allocation row-wise"
    (let [return-ds (ds-ordered [1 2] [[:A [0.10 -0.10]] [:B [0.00 0.20]]])
          weight-ds (ds-ordered [1 2] [[:A [0.5 0.5]] [:B [0.5 0.5]]])
          pr (pa/portfolio-return return-ds weight-ds)]
      (is (= [:date "A" "B"] (tc/column-names pr)))
      (is (= [1 2] (into [] (tc/column pr :date))))
      ;; row 1: 0.1*0.5=0.05, 0*0.5=0
      ;; row 2: -0.1*0.5=-0.05, 0.2*0.5=0.1
      (is (= [0.05 -0.05] (into [] (tc/column pr "A"))))
      (is (= [0.0 0.10] (into [] (tc/column pr "B"))))))
  (testing "leveraged weights (>1 per asset or row) scale the weighted return"
    (let [return-ds (ds-ordered [1] [[:A [0.10]]])
          weight-ds (ds-ordered [1] [[:A [2.0]]])
          pr (pa/portfolio-return return-ds weight-ds)]
      (is (= [0.20] (into [] (tc/column pr "A"))))))
  (testing "portfolio-cumulative-returns compounds per-asset columns and :portfolio from row sums"
    (let [pr (ds-ordered [1 2] [[:A [0.05 -0.05]] [:B [0.0 0.10]]])
          cum (pa/portfolio-cumulative-returns pr)
          tol 1e-9]
      (is (= [:date "A" "B" :portfolio] (tc/column-names cum)))
      ;; A: (1+0.05)-1 = 0.05 ; (1.05)(0.95)-1 = -0.0025
      (is (col-near? [0.05 -0.0025] (into [] (tc/column cum "A")) tol))
      ;; B: 0 then (1)(1.1)-1 = 0.1
      (is (col-near? [0.0 0.10] (into [] (tc/column cum "B")) tol))
      ;; period portfolio return = sum per row: 0.05 and 0.05
      ;; cum: (1.05)-1=0.05 ; (1.05)(1.05)-1 = 0.1025
      (is (col-near? [0.05 0.1025] (into [] (tc/column cum :portfolio)) tol)))))
