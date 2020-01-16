(ns io.jesi.customs.strict.kaocha-test
  (:refer-clojure :exclude [=])
  (:require
    [io.jesi.customs.strict :refer :all]))

(deftest ^:kaocha/skip kaocha-diff-test
  (is= {:a 1} {:a 2}))
