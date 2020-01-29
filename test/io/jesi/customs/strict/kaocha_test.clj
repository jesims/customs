(ns io.jesi.customs.strict.kaocha-test
  (:refer-clojure :exclude [=])
  (:require
    [io.jesi.customs.strict :refer :all]))

;skipped since couldn't intercept kaocha reporting
(deftest ^:kaocha/skip kaocha-diff-test
  ;TODO intercept kaocha reporting instead of having a failing test
  (is= {:a 1} {:a 2}))
