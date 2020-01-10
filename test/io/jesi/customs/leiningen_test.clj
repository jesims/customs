(ns io.jesi.customs.leiningen-test
  (:refer-clojure :exclude [=])
  (:require
    [io.jesi.backpack :as bp]
    [io.jesi.backpack.macros :refer [def-]]
    [io.jesi.customs.leiningen :refer :all]
    [io.jesi.customs.strict :refer :all]
    [io.jesi.customs.util :refer [pprint-str]]
    [leiningen.compile :refer [regex?]]))

(def- aot-project-path "test-projects/aot/project.clj")

(deftest find-gen-class-ns-test

  (testing "find-gen-class-ns"
    (is= {'gen-class-example   {:name 'GenClassExample},
          'gen-class-example-2 {:name 'GenClassExample2
                                :main false}}
         (find-gen-class-ns (read-project aot-project-path)))))

(deftest is-gen-class-in-aot-test

  (testing "is-gen-class-in-aot"
    (is-gen-class-in-aot (read-project aot-project-path [:install]))))

(deftest list-jar-test

  (testing "list-jar"
    (let [paths (-> aot-project-path read-project build-jar list-jar set)]
      (doseq [required ["META-INF/"
                        "META-INF/MANIFEST.MF"
                        "META-INF/leiningen/aot-test/aot-test/project.clj"
                        "META-INF/maven/"
                        "META-INF/maven/aot-test/"
                        "META-INF/maven/aot-test/aot-test/"
                        "META-INF/maven/aot-test/aot-test/pom.properties"
                        "META-INF/maven/aot-test/aot-test/pom.xml"
                        "GenClassExample.class"
                        #"gen_class_example\$.+\.class"
                        #"gen_class_example\$loading__.+\.class"
                        "gen_class_example.clj"
                        "gen_class_example_2.clj"
                        "gen_class_example__init.class"
                        "normal.clj"]]
        (let [error-msg (str "Could not find " required " in " (pprint-str paths))]
          (if (regex? required)
            (is (some (partial re-matches required) paths) error-msg)
            (is (some (bp/p= required) paths) error-msg)))))))
