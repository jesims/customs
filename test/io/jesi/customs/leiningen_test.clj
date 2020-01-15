(ns io.jesi.customs.leiningen-test
  (:refer-clojure :exclude [=])
  (:require
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

(deftest list-jar-test

  (testing "list-jar"
    (let [paths (-> project lein/build-jar lein/list-jar set)
          ;TODO automatic normalisation for auto symbols .e.g is-macro=
          required ["META-INF/"
                    "META-INF/MANIFEST.MF"
                    "META-INF/leiningen/io.jesi/aot-test/project.clj"
                    "META-INF/maven/"
                    "META-INF/maven/io.jesi/"
                    "META-INF/maven/io.jesi/aot-test/"
                    "META-INF/maven/io.jesi/aot-test/pom.properties"
                    "META-INF/maven/io.jesi/aot-test/pom.xml"
                    "GenClassExample.class"
                    #"gen_class_example\$fn__.+\.class"
                    #"gen_class_example\$loading__.+__auto____.+\.class"
                    "gen_class_example.clj"
                    "gen_class_example_2.clj"
                    "gen_class_example__init.class"
                    "normal.clj"
                    "secret/"
                    #"secret/clearance\$fn__.+\.class"
                    #"secret/clearance\$loading__.+__auto____.+\.class"
                    "secret/clearance.class"
                    "secret/clearance.clj"
                    "secret/clearance__init.class"
                    "secret/top/"
                    "secret/top/EyesOnly.class"
                    #"secret/top/eyes_only\$fn__.+\.class"
                    #"secret/top/eyes_only\$loading__.+__auto____.+\.class"
                    "secret/top/eyes_only.clj"
                    "secret/top/eyes_only__init.class"
                    "FILE"]]
      (is= (count paths)
           (count required))
      (doseq [required required]
        (let [error-msg (str "Could not find " required " in " (pprint-str paths))]
          (if (regex? required)
            (is (some (partial re-matches required) paths) error-msg)
            (is (some (bp/p= required) paths) error-msg)))))))

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
           (lein/find-gen-class-paths (read-project))))))

(deftest is-jar-contains-test

  (testing "is-jar-contains"

    (testing "checks the jar contains aot and extra files"
      (lein/is-jar-contains project-with-install "FILE"))))
