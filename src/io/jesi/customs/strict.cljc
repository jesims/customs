(ns io.jesi.customs.strict
  (:refer-clojure :exclude [=])
  #?(:cljs (:require-macros [io.jesi.customs.strict :refer [is]]))
  (:require
    [clojure.test :as test]
    [io.jesi.backpack.env :as env]
    [io.jesi.backpack.string :refer [not-blank?]]))

#?(:cljs (enable-console-print!))

(defn =
  #?(:clj
     "Equality. Takes at least 2 args. Returns `true` if `x` equals `y`, `false` if not.
     Same as Java `x.equals(y)` except it also works for `nil`, and compares numbers
     and collections in a type-independent manner. Clojure's immutable data structures
     define `equals()` (and thus `=`) as a value, not an identity, comparison."
     :default
     "Equality. Takes at least 2 args. Returns `true` if `x` equals `y`, `false` if not.
     Compares numbers and collections in a type-independent manner. Clojure's immutable
     data structures define `-equiv` (and thus `=`) as a value, not an identity, comparison.")
  [x y & more]
  (if (seq more)
    (apply clojure.core/= x y more)
    (clojure.core/= x y)))

(defmacro is
  "Generic assertion macro. `form` is any predicate test.
  `msg` is an optional message to attach to the assertion.
  Will fail is message is not a string.

  Example: `(is (= 4 (+ 2 2)) \"Two plus two should be 4\")`

  Special forms:

  `thrown?` checks that an instance of `c` is thrown from
  body, fails if not; then returns the thing thrown.

  `thrown-with-msg?` checks that an instance of `c` is
  thrown from `body` and that the message on the exception
  matches (with `re-find`) the regular expression `re`."
  ([form]
   {:pre [(some? form)]}
   (let [is (env/symbol &env `test/is)]
     `(~is ~form)))
  ([form msg]
   {:pre [(some? form)
          (some? msg)]}
   (let [is (env/symbol &env `test/is)]
     `(do
        (assert (not-blank? ~msg))
        (~is ~form ~msg)))))

(defmacro is= [x y & more]
  (let [is (env/symbol &env `test/is)]
    `(~is (clojure.core/= ~x ~y ~@more))))

(defn- default-body [env body]
  (if (seq body)
    body
    (let [try-expr (env/symbol env `test/try-expr)]
      [`(~try-expr "Test is empty" nil)])))

(defmacro deftest
  "Like `clojure.test/deftest`, but will fail if `body` is empty."
  [name & body]
  {:pre [(symbol? name)]}
  (let [deftest (env/symbol &env `test/deftest)]
    `(~deftest ~name
       ~@(default-body &env body))))

(defmacro testing
  "Like `clojure.test/testing`, but will fail if `body` is empty."
  [string & body]
  {:pre [(some? string)]}
  (let [testing (env/symbol &env `test/testing)]
    `(~testing ~string
       (assert (not-blank? ~string))
       ~@(default-body &env body))))

(defn thrown?
  "Checks that an instance of `c` is thrown from `body`, fails if not;
  then returns the thing thrown. Must be used in an `is` block."
  [c body]
  (assert nil "`thrown?` used not in `is` block"))

(defn thrown-with-msg?
  "Checks that an instance of `c` is thrown AND that the message
  on the exception matches (with re-find) the regular expression `re`.
  Must be used in an `is` block."
  [c re body]
  (assert nil "`thrown-with-msg?` used not in `is` block"))

(defmacro are [argv expr & args]
  {:pre [(seq argv)
         (some? expr)
         (seq args)]}
  (let [are (env/symbol &env `test/are)]
    `(~are ~argv ~expr ~@args)))

(defmacro use-fixtures
  "Wrap test runs in a fixture function to perform setup and
  teardown. Using a fixture-type of :each wraps every test
  individually, while :once wraps the whole run in a single function."
  [type & fns]
  {:pre [(#{:once :each} type)
         (seq fns)]}
  (let [use-fixtures (env/symbol &env `test/use-fixtures)]
    `(~use-fixtures ~type ~@fns)))
