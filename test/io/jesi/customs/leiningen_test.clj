(ns io.jesi.customs.leiningen-test
  (:refer-clojure :exclude [=])
  (:require
    [clojure.string :as str]
    [io.jesi.backpack :as bp]
    [io.jesi.backpack.macros :refer [def-]]
    [io.jesi.customs.leiningen :as lein]
    [io.jesi.customs.strict :refer :all]
    [io.jesi.customs.util :refer [pprint-str]]
    [leiningen.compile :refer [regex?]]))

(def- read-project (partial lein/read-project "test-projects/aot/project.clj"))
(def- project (read-project))
(def- project-with-install (read-project [:install]))



(deftest find-gen-class-ns-test

  (testing "find-gen-class-ns"
    (is= {'gen-class-example    {:name 'GenClassExample},
          'gen-class-example-2  {:name 'GenClassExample2
                                 :main false}
          'secret.top.eyes-only {:name 'secret.top.EyesOnly}
          'secret.clearance     {}}
         (lein/find-gen-class-ns project))))

(deftest is-gen-class-in-aot-test

  (testing "is-gen-class-in-aot"
    (lein/is-gen-class-in-aot project-with-install)))

(deftest expected-meta-files-test

  (testing "expected-meta-files"
    (let [actual (lein/expected-meta-files project)]

      (testing "returns a vector"
        (is (seq actual))
        (is (vector? actual))

        (testing "of"

          (testing "strings"
            (is (every? string? actual)))

          (testing "paths for the usual meta data found in a .jar"
            (is= ["META-INF/"
                  "META-INF/MANIFEST.MF"
                  "META-INF/leiningen/io.jesi/aot-test/project.clj"
                  "META-INF/maven/"
                  "META-INF/maven/io.jesi/"
                  "META-INF/maven/io.jesi/aot-test/"
                  "META-INF/maven/io.jesi/aot-test/pom.properties"
                  "META-INF/maven/io.jesi/aot-test/pom.xml"]
                 actual)))))))

(deftest find-gen-class-paths-test

  (testing "find-gen-class-paths"

    (testing "returns paths to gen-class classes"
      (is= ["GenClassExample.class"
            "GenClassExample2.class"
            "secret/clearance.class"
            "secret/top/EyesOnly.class"]
           (lein/find-gen-class-paths project)))))

(deftest is-jar-contains-required-test

  (testing "is-jar-contains-required"

    (testing "checks the jar contains AoT and extra files"
      (lein/is-jar-contains-required project-with-install "FILE"))))

(deftest is-jar-paths-match-test

  (testing "is-jar-paths-match"

    (testing "checks the entries in the jar match the given `pred`"
      (let [gen-class-path (set (lein/find-gen-class-paths project-with-install))]
        (lein/is-jar-paths-match project-with-install
          (let [starts-with? #(bp/partial-right str/starts-with? %)
                = bp/p=]
            (bp/or-fn
              (starts-with? "META-INF/")
              (partial contains? gen-class-path)
              (= "FILE"))))))))
