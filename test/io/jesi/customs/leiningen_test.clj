(ns io.jesi.customs.leiningen-test
  (:refer-clojure :exclude [=])
  (:require
    [clojure.java.io :as io]
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

(deftest build-jar-test

  (testing "build-jar"

    (testing "deletes target/classes dir when done"
      (let [jar-path (lein/build-jar project-with-install)]
        (is (seq jar-path))
        (is (-> jar-path io/file (.exists)))
        (is (not (-> project-with-install :target-path (str "/classes") io/file (.exists))))))))

(deftest is-slim-jar-test

  (testing "is-slim-jar"
    (lein/is-slim-jar project-with-install)))

(deftest deps-test

  (testing "deps"
    (let [lein-dependency 'leiningen/leiningen
          contains-leiningen? (fn [[dependency version]]
                                (and (= lein-dependency dependency)
                                     (= "2.9.6" version)))]

      (testing "returns a vector of dependencies"
        (let [actual (lein/deps)]
          (is (some contains-leiningen? actual))

          (testing "including managed dependency versions"
            (is (some (fn [[dependency version]]
                        (and (= 'org.clojure/clojure dependency)
                             (string? version)
                             (str/starts-with? version "1.")))
                  actual)))))

      (testing "can exclude dependencies"
        (let [actual (lein/deps lein-dependency)]
          (is (not (some contains-leiningen? actual))))))))
