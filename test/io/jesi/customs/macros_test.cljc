(ns io.jesi.customs.macros-test
  (:refer-clojure :exclude [=])
  (:require
    #?(:clj [io.jesi.backpack.macros :refer [macro?]])
    [io.jesi.backpack.async :as async]
    [io.jesi.customs.macros :refer [async-go]]
    [io.jesi.customs.strict :refer [= deftest is is= testing]]
    [io.jesi.customs.util :refer [is-macro=]]))

(deftest async-go-test

  (testing "async-go"

    #?(:clj (testing "is a macro"
              (is (macro? `async-go))))

    (testing "expands"
      (is= #?(:clj  '(clojure.core.async/<!!
                       (clojure.core.async/go
                         (is true)))
              :cljs '(cljs.test/async done
                       (cljs.core.async/go
                         (try
                           (is true)
                           (finally (done))))))
           (macroexpand-1 '(io.jesi.customs.macros/async-go (is true)))))

    (testing "is a `clojure.test/async` `go` block"
      (async-go
        (is= 1 (async/<? (async/go 1)))))))
