(ns io.jesi.backpack.string
  (:require
    [clojure.string :as string]))

(defn uuid-str? [s]
  (and (string? s) (re-matches #"(\w{8}(-\w{4}){3}-\w{12}?)$" s)))

(defn subs-to
  "Returns the substring of 's' up until the 'match'"
  [match s]
  (let [index (string/index-of s match)]
    (if (nil? index)
      s
      (subs s 0 index))))

(defn subs-inc
  "Returns the substring of 's' up to and including the 'match' or nil"
  [match s]
  (let [index (string/index-of s match)]
    (if (nil? index)
      nil
      (subs s 0 (+ index (count match))))))

(defn true-string?
  [s]
  (= s "true"))

(defn ->camelCase [s]
  (when s
    (let [[head & tail] (string/split (name s) #"-|(?=[A-Z])")]
      (string/join (cons (string/lower-case head) (map string/capitalize tail))))))

(defn ->kebab-case [s]
  (some-> s
          name
          (string/split #"(?<=[A-Z])(?=[A-Z][a-z])|(?<=[^A-Z])(?=[A-Z])|\s|(?<=.)_")
          (->> (string/join "-"))
          (string/lower-case)))

(def ->kebab-case-key (comp keyword ->kebab-case))
(def ->camelCase-key (comp keyword ->camelCase))
