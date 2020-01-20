(ns io.jesi.customs.leiningen
  (:require
    [clojure.java.io :as io]
    [clojure.tools.namespace.file :as ns-file]
    [clojure.tools.namespace.find :as ns-find]
    [com.rpl.specter :as sp]
    [io.jesi.backpack :as bp]
    [io.jesi.backpack.macros :refer [def-]]
    [io.jesi.customs.strict :refer [is is=]]
    [leiningen.core.main]
    [leiningen.core.project :as project]
    [leiningen.jar])
  (:import
    (java.io File StringWriter)
    (java.util.zip ZipInputStream)))

;TODO add ability to clear memoization cache

(defn read-project
  ([] (read-project "project.clj"))
  ([file] (read-project file [:default]))
  ([file profiles]
   (project/ensure-dynamic-classloader)
   (project/read file profiles)))

(def ^{:arglists '([project])} find-gen-class-ns
  "Returns a map of ns symbols and its :gen-class definition (as a map)"
  (memoize
    (fn find-gen-class-ns-fn [project]
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
           (apply merge)))))

;TODO use prolog (core.logic) based derived value calculation
; - register the transformations (e.g. nil to project, project to gen-class-ns)
; - add "type" information to result
; - defined the function with that it needs (e.g. I take whatever, but need gen-class-ns. it get's converted automatically)
(defn find-gen-class-paths
  "Returns the paths for gen-class namespaces"
  [project]
  (->> project
       find-gen-class-ns
       (map (fn [[ns {:keys [name]}]]
              (-> (or name ns)
                  str
                  (.replace \. \/)
                  (str ".class"))))))

(defn is-gen-class-in-aot [project]
  (let [aot (:aot project)]
    (when (is (seq aot))
      (is= (sort aot)
           (sort (keys (find-gen-class-ns project)))))))

;TODO move to backpack?
(defn- delete-dir [& dirs]
  ;from https://gist.github.com/edw/5128978#gistcomment-2956766
  (when-let [f (first dirs)]
    (if-let [files (-> f io/file (.listFiles) seq)]
      (recur (concat files dirs))
      (do
        (io/delete-file f)
        (recur (rest dirs))))))

(def ^{:arglists '([project])} build-jar
  "Runs the `jar` command, returning the path of the built jar"
  (memoize
    (fn build-jar-fn [project]
      (let [out (let [s (new StringWriter)]
                  (binding [*out* s
                            *err* s
                            leiningen.core.main/*info* true
                            leiningen.core.main/*exit-process?* false]
                    (leiningen.jar/jar project))
                  (-> s str .trim))]
        (delete-dir (str (:target-path project) "/classes"))
        (some->> out (re-find #"(?<=Created ).+") str .trim)))))

(defn- list-zip [file]
  (with-open [zip-stream (ZipInputStream. (io/input-stream file))]
    (loop [paths (transient [])]
      (if-let [entry (-> zip-stream .getNextEntry)]
        (recur (conj! paths (-> entry .getName)))
        (persistent! paths)))))

(def ^{:arglists '([project])} list-jar
  "Build and list (as a vector of strings) the contents of the .jar"
  (memoize
    (fn list-jar-fn [project]
      (some-> project build-jar list-zip))))

(defn expected-meta-files
  "Returns paths for the usual meta data found in a .jar"
  [{:keys [name group] :as project}]
  (let [path (str group \/ name \/)
        meta "META-INF/"
        meta-maven (str meta "maven/")]
    [meta
     (str meta "MANIFEST.MF")
     (str meta "leiningen/" path "project.clj")
     meta-maven
     (str meta-maven group "/")
     (str meta-maven path)
     (str meta-maven path "pom.properties")
     (str meta-maven path "pom.xml")]))

(def- clojure-file-extensions (set (concat ns-file/clojure-extensions ns-file/clojurescript-extensions)))

(defn- clojure-ns-paths [project]
  (apply concat
    (for [dir (:source-paths project)
          :let [dir (io/file dir)
                dir-path (-> dir .toPath)]]
      (->> dir
           file-seq
           (filter (bp/partial-right ns-file/file-with-extension? clojure-file-extensions))
           (map (fn [^File f]
                  (-> dir-path
                      (.relativize (-> f .toPath))
                      str)))))))

(defn is-slim-jar
  "Builds the project .jar and checks it only contains the required
  `.class`, `.clj`, and `META-INF` files.
  Use with leiningen `:jar-includes` and `:jar-excludes` `project.clj` settings."
  [project & other-files]
  (let [expected (sort (concat
                         (expected-meta-files project)
                         (find-gen-class-paths project)
                         (clojure-ns-paths project)
                         other-files))
        actual (sort (list-jar project))]
    (is= expected actual)))
