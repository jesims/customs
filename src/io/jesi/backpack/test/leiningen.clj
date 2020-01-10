(ns io.jesi.backpack.test.leiningen
  (:require
    [clojure.java.io :as io]
    [clojure.tools.namespace.find :as ns-find]
    [com.rpl.specter :as sp]
    [io.jesi.backpack :as bp]
    [io.jesi.backpack.test.strict :refer [is is=]]
    [leiningen.core.main]
    [leiningen.core.project :as project]
    [leiningen.jar])
  (:import
    (java.io StringWriter)
    (java.util.zip ZipInputStream)))

(defn read-project
  ([file] (read-project file [:default]))
  ([file profiles]
   (project/ensure-dynamic-classloader)
   (project/read file profiles)))

(defn find-gen-class-ns
  "Returns a map of ns symbols and it's :gen-class definition (as a map)"
  [project]
  (->> (for [dir (:source-paths project)]
         (->> dir
              io/file
              ns-find/find-ns-decls-in-dir
              (map (fn [ns-decl]
                     (when-let [gen-class (sp/select-one [sp/ALL (bp/and-fn seqable? (bp/compr first (bp/p= :gen-class)))] ns-decl)]
                       [(second ns-decl)
                        (->> gen-class
                             rest
                             (apply hash-map))])))
              (into {})))
       (apply merge)))

(defn is-gen-class-in-aot [project]
  (let [aot (:aot project)]
    (when (is (seq aot))
      (is= aot
           (keys (find-gen-class-ns project))))))

(defn build-jar
  "Runs the `jar` command, returning the path of the built jar"
  [project]
  (let [out (let [s (new StringWriter)]
              (binding [*out* s
                        *err* s
                        leiningen.core.main/*info* true
                        leiningen.core.main/*exit-process?* false]
                (leiningen.jar/jar project))
              (-> s str .trim))]
    (println out)
    (some->> out (re-find #"(?<=Created ).+") str .trim)))

;TODO move to backpack?
(defn- list-zip [file]
  (with-open [zip-stream (ZipInputStream. (io/input-stream file))]
    (loop [paths (transient [])]
      (if-let [entry (-> zip-stream .getNextEntry)]
        (recur (conj! paths (-> entry .getName)))
        (persistent! paths)))))

(defn list-jar [jar-path]
  (some-> jar-path list-zip seq))
