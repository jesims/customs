(ns io.jesi.customs.kaocha-test
  (:refer-clojure :exclude [=])
  (:require
    [clojure.string :as str]
    [io.jesi.backpack.random :as rnd]
    [io.jesi.customs.kaocha :as kaocha]
    [io.jesi.customs.strict :refer :all]
    [io.jesi.customs.util :as util])
  (:import
    (java.io ByteArrayOutputStream)))

(util/when-provided? 'kaocha.plugin.capture-output

  (deftest reset-output-test

    (testing "clears kaocha test output buffer"
      (let [test-str (rnd/string)
            includes-test-str? #(-> 'kaocha.plugin.capture-output/*test-buffer* resolve ^ByteArrayOutputStream deref .toString
                                    (str/includes? test-str))]
        (println test-str)
        (is (includes-test-str?))
        (kaocha/reset-output)
        (is (not (includes-test-str?)))))))
