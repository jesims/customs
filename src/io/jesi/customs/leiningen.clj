(ns io.jesi.customs.leiningen
  (:require
    [clojure.java.io :as io]
    [clojure.tools.namespace.find :as ns-find]
    [com.rpl.specter :as sp]
    [io.jesi.backpack :as bp]
    [io.jesi.customs.strict :refer [is is=]]
    [leiningen.core.main]
    [leiningen.core.project :as project]
    [leiningen.jar])
  (:import
    (java.io StringWriter)
    (java.util.zip ZipInputStream)))

(defn- set-type [type o]
  (vary-meta o
    assoc ::type type))


(defn read-project
  ([] (read-project "project.clj"))
  ([file] (read-project file [:default]))
  ([file profiles]
   (project/ensure-dynamic-classloader)
   (->> (project/read file profiles)
        (set-type :project))))

(defn find-gen-class-ns
  "Returns a map of ns symbols and it's :gen-class definition (as a map)"
  ([] (find-gen-class-ns (read-project)))
  ([project]
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
        (apply merge)
        (set-type :gen-class-namespaces))))

;TODO use prolog (core.logic) based derived value calculation
; - register the transformations (e.g. nil to project, project to gen-class-ns)
; - add "type" information to result
; - defined the function with that it needs (e.g. I take whatever, but need gen-class-ns. it get's converted automatically)
(defn find-gen-class-paths
  "Returns the paths for gen-class namespaces"
  ([] (find-gen-class-paths (read-project)))
  ([project-or-gen-class-namespaces]
   (case (-> project-or-gen-class-namespaces meta ::type)
     :project (recur (find-gen-class-ns project-or-gen-class-namespaces))
     :gen-class-namespaces (->> project-or-gen-class-namespaces
                                (map (fn [[ns {:keys [name]}]]
                                       (-> (or name ns)
                                           str
                                           (.replace \. \/)
                                           (str ".class"))))))))

(defn is-gen-class-in-aot
  ([] (is-gen-class-in-aot (read-project)))
  ([project]
   (let [aot (:aot project)]
     (when (is (seq aot))
       (is= (sort aot)
            (sort (keys (find-gen-class-ns project))))))))

(defn build-jar
  "Runs the `jar` command, returning the path of the built jar"
  ([] (build-jar (read-project)))
  ([project]
   (let [out (let [s (new StringWriter)]
               (binding [*out* s
                         *err* s
                         leiningen.core.main/*info* true
                         leiningen.core.main/*exit-process?* false]
                 (leiningen.jar/jar project))
               (-> s str .trim))]
     (println out)
     (some->> out (re-find #"(?<=Created ).+") str .trim))))

;TODO move to backpack?
(defn- list-zip [file]
  (with-open [zip-stream (ZipInputStream. (io/input-stream file))]
    (loop [paths (transient [])]
      (if-let [entry (-> zip-stream .getNextEntry)]
        (recur (conj! paths (-> entry .getName)))
        (persistent! paths)))))

(defn list-jar
  ([]
   (-> (read-project) build-jar list-jar))
  ([jar-path]
   (some-> jar-path list-zip)))

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

(defn is-jar-contains [project & other-files]
  (let [zip-paths (-> project build-jar list-jar set)]
    (when (is (seq zip-paths))
      (doseq [required (concat
                         (expected-meta-files project)
                         (find-gen-class-paths project)
                         other-files)]
        (is (contains? zip-paths required) (str required " not found in jar"))))))
