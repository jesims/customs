(ns io.jesi.customs.macros
  #?(:cljs (:require-macros [io.jesi.customs.macros]))
  (:require
    [clojure.core.async]
    [clojure.test :as test]
    [io.jesi.backpack.async :as async]
    [io.jesi.backpack.env :as env]))

(defmacro async-go [& body]
  (if (env/cljs? &env)
    `(cljs.test/async ~'done
       (cljs.core.async/go
         (try
           ~@body
           (finally
             (~'done)))))
    `(clojure.core.async/<!! (clojure.core.async/go
                               ~@body))))

(defmacro is-nil<? [body]
  (let [is (env/symbol &env `test/is)]
    `(~is (nil? (async/<? ~body)))))
