(ns io.jesi.customs.spy-test
  #?(:cljs (:require-macros [io.jesi.customs.spy-test :refer [cljs? set-line]]))
  (:refer-clojure :exclude [= ns-name])
  (:require
    [io.jesi.backpack :as bp]
    [io.jesi.backpack.env :as env]
    [io.jesi.backpack.macros :refer [shorthand]]
    [io.jesi.customs.spy :as spy]
    [io.jesi.customs.strict :refer [= deftest is is= testing use-fixtures]]
    [io.jesi.customs.util :refer [is-macro=]]))

(defn- set-debug [v]
  #?(:cljs (set! js/goog.DEBUG v)))

(use-fixtures :each
  (fn [f]
    (f)
    (set-debug false)))

(def file #?(:clj  *file*
             :cljs (-> #'set-debug meta :file)))

(def ns-name (str 'io.jesi.customs.spy-test))

(def line (atom nil))

(defmacro set-line [offset]
  (let [n (+ (-> &form meta :line) offset)]
    `(str \: (reset! line ~n))))

(defn- add-line [offset]
  (str \: (swap! line + offset)))

(def a 1)
(def b 2)
(def c (shorthand a b))

(deftest prn-test

  (testing "spy/prn"

    #?(:clj (testing "is a macro"
              (bp/macro? `spy/prn)))

    (testing "prns"
      (spy/enabled
        (set-debug true)

        (testing "the specified values"
          (is= (str file (set-line 1) " a: 1" \newline)
               (with-out-str (spy/prn a)))
          (is= (str file (add-line 2) " a: 1 b: 2" \newline)
               (with-out-str (spy/prn a b))))

        (testing "literal expressions"
          (is= (str file (add-line 4) " 1: 1" \newline)
               (with-out-str (spy/prn 1)))
          (is= (str file (add-line 2) " a: \"a\"" \newline)
               (with-out-str (spy/prn "a")))
          (is= (str file (add-line 2) " (inc 1): 2" \newline)
               (with-out-str (spy/prn (inc 1))))
          (is= (str file (add-line 2) " ((comp inc dec) 1): 1" \newline)
               (with-out-str (spy/prn ((comp inc dec) 1))))))

      (testing "nothing when not"

        #?(:cljs (testing "debug"
                   (set-debug false)
                   (is (empty? (with-out-str (spy/prn a))))
                   (is (empty? (with-out-str (spy/prn a b))))))

        (testing "enabled"
          (is (empty? (with-out-str (spy/prn a)))))))))

(deftest pprint-test

  (testing "spy/pprint"

    #?(:clj (testing "is a macro"
              (bp/macro? `spy/pprint)))

    (testing "pprints"
      (spy/enabled
        (set-debug true)

        (testing "the specified values"
          (is= (str file (set-line 2) " a:" \newline
                 "1" \newline)
               (with-out-str (spy/pprint a)))
          (is= (str file (add-line 5) " a:" \newline
                 "1" \newline
                 file (str ":" @line) " b:" \newline
                 "2" \newline)
               (with-out-str (spy/pprint a b)))
          (is= (str file (add-line 3) " c:" \newline
                 "{:a 1, :b 2}" \newline)
               (with-out-str (spy/pprint c))))

        ;cljs messes up the formatting, it adds a space after the :d line
        #?(:clj (testing "literal expressions"
                  (let [val {:a 0 :b 1 :c 2 :d 3 :e 4}]
                    (is= (str file (add-line 12) \space
                           "{:a val, :b val, :c val, :d val, :e val}:" \newline
                           "{:a {:a 0, :b 1, :c 2, :d 3, :e 4}," \newline
                           " :b {:a 0, :b 1, :c 2, :d 3, :e 4}," \newline
                           " :c {:a 0, :b 1, :c 2, :d 3, :e 4}," \newline
                           " :d {:a 0, :b 1, :c 2, :d 3, :e 4}," \newline
                           " :e {:a 0, :b 1, :c 2, :d 3, :e 4}}" \newline)
                         (with-out-str (spy/pprint {:a val :b val :c val :d val :e val})))))))

      (testing "nothing when not"

        #?(:cljs (testing "debug"
                   (set-debug false)
                   (is (empty? (with-out-str (spy/pprint a))))
                   (is (empty? (with-out-str (spy/pprint a b))))
                   (is (empty? (with-out-str (spy/pprint c))))))

        (testing "enabled"
          (is (empty? (with-out-str (spy/pprint a)))))))))

(defmacro cljs? []
  (env/cljs? &env))

(deftest peek-test

  (testing "peek"

    #?(:clj (testing "is a macro"
              (bp/macro? `spy/peek)))

    #?(:clj (comment                                        ;Fails since Actual is a vector (not a list)
              (testing "expands"
                (is-macro= '(io.jesi.backpack.macros/when-debug
                              (when io.jesi.customs.spy/*enabled*
                                (let [v# (inc 1)]
                                  (println "user:138 (inc 1):" (pr-str v#))
                                  v#)))
                           (macroexpand-1 '(io.jesi.customs.spy/peek (inc 1)))))))

    (testing "prns (using spy/prn) and return the passed in value"
      (spy/enabled
        (set-debug true)
        (let [result (atom nil)]
          (is= (str file (set-line 1) " a: 1" \newline)
               (with-out-str (reset! result (spy/peek a))))
          (is= a @result)

          (testing "even in a thread macro (no line numbers since the &from metadata is not preserved)"
            (let [file (if (cljs?) ns-name file)]
              (is= (str file " a: 1" \newline)
                   (with-out-str (reset! result (-> a spy/peek inc))))
              (is= (inc a) @result))))))

    (testing "passes the value through even when disabled"
      (let [v (rand-int 10)]
        (is= v (spy/peek v))))

    (testing "evaluates the form once"
      (spy/enabled
        (set-debug true)
        (let [calls (atom 0)
              f (fn []
                  (swap! calls inc))]
          (is= 1 (spy/peek (f)))
          (is= 1 @calls))))))

(deftest ppeek-test

  (testing "ppeek"

    #?(:clj (testing "is a macro"
              (bp/macro? `spy/ppeek)))

    (testing "pretty prints (using spy/pprint) and return the passed in value"
      (spy/enabled
        (set-debug true)
        (let [result (atom nil)]
          (is= (str file (set-line 1) " a:" \newline "1" \newline)
               (with-out-str (reset! result (spy/ppeek a))))
          (is= a @result)

          (testing "even in a thread macro (no line numbers since the &from metadata is not preserved)"
            (let [file (if (cljs?) ns-name file)]
              (is= (str file " a:" \newline "1" \newline)
                   (with-out-str (reset! result (-> a spy/ppeek inc))))
              (is= (inc a) @result))))))

    (testing "passes the value through even when disabled"
      (let [v (rand-int 10)]
        (is= v (spy/ppeek v))))

    (testing "evaluates the form once"
      (spy/enabled
        (set-debug true)
        (let [calls (atom 0)
              f (fn []
                  (swap! calls inc))]
          (is= 1 (spy/ppeek (f)))
          (is= 1 @calls))))))

(deftest msg-test

  (testing "msg"

    #?(:clj (testing "is a macro"
              (bp/macro? `spy/msg)))

    (testing "print a message"
      (spy/enabled
        (set-debug true)
        (let [msg "There should be a theme"]
          (is= (str file (set-line 1) ": " msg \newline)
               (with-out-str (spy/msg msg))))))))
