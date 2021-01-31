(ns io.jesi.customs.spy
  (:refer-clojure :exclude #?(:clj  [peek prn]
                              :cljs [-name peek prn -peek]))
  #?(:cljs (:require-macros [io.jesi.customs.spy :refer [pprint prn]]))
  (:require
    [clojure.string :as str]
    [io.jesi.backpack.collection :refer [trans-reduce]]
    [io.jesi.backpack.macros :refer [when-debug]]
    [io.jesi.customs.util :refer [pprint-str]]))

(def ^:dynamic *enabled* false)

(defmacro enabled [& body]
  `(binding [*enabled* true]
     ~@body))

(defn- -name [form]
  (if (symbol? form)
    (name form)
    (str form)))

(defn- line-number [file form]
  (let [{:keys [line] meta-file :file} (meta form)
        f (or (when (and file
                         (not= "NO_SOURCE_PATH" file)
                         (not= \/ (nth file 0)))
                file)
              meta-file
              *ns*)]
    (if line
      (str f \: line)
      (str f))))

(defn- -prn [file form & more]
  `(when-debug
     (when *enabled*
       (println ~@(let [line (line-number file form)]
                    (trans-reduce
                      (fn [col form]
                        (-> col
                            (conj! (str (-name form) ":"))
                            (conj! `(pr-str ~form))))
                      [line]
                      more))))))

(defn- -pprint [file form & more]
  `(when-debug
     (when *enabled*
       (do ~@(let [line (line-number file form)]
               (for [form more]
                 `(println (str ~(str line \space (-name form) \: \newline) (pprint-str ~form)))))))))

(defn- -peek [file form val]
  `(when-debug
     (when *enabled*
       (let [v# ~val]
         (println ~(str (line-number file form) \space (-name val) \:) (pr-str v#))
         v#))))

(defn- -ppeek [file form val]
  `(when-debug
     (when *enabled*
       (let [v# ~val]
         (println (str ~(str (line-number file form) \space (-name val) \: \newline) (pprint-str v#)))
         v#))))

(defn- -msg [file form & more]
  `(when-debug
     (when *enabled*
       (println ~(str (line-number file form) ":") ~@more))))

(defmacro prn [& more]
  #?(:clj (apply -prn *file* &form more)))

(defmacro pprint [& more]
  #?(:clj (apply -pprint *file* &form more)))

(defmacro peek [val]
  #?(:clj (-peek *file* &form val)))

(defmacro ppeek [val]
  #?(:clj (-ppeek *file* &form val)))

(defmacro msg [& more]
  #?(:clj (apply -msg *file* &form more)))
