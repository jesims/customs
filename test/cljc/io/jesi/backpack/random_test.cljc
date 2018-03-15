(ns io.jesi.backpack.random-test
  (:require
    [clojure.test :refer [deftest testing is]]
    [io.jesi.backpack :as bp]
    [io.jesi.backpack.random :as rnd]))

(defn- assert-random [fn]
  (let [actuals (take 1000 (repeatedly fn))]
    (is (= (count actuals) (count (set actuals))))
    actuals))

(deftest uuid-test
  (testing "UUID's are always random"
    (assert-random #(rnd/uuid))))

(deftest uuid-str-test
  (testing "UUID strings are always random"
    (let [actuals (assert-random #(rnd/uuid-str))
          non-uuid-strs (filter (comp not bp/uuid-str?) actuals)]
      (is (nil? (seq non-uuid-strs))))))

(deftest string-test
  (testing "Strings are always random"
    (assert-random #(rnd/string)))

  (testing "Can create a random string of size"
    (let [size (rand-int 2000)
          actual (rnd/string size)]
      (is (= size (count actual))))))
