(ns io.jesi.backpack.miscellaneous
  (:refer-clojure :exclude [assoc-in])
  (:require
    [io.jesi.backpack.collection :refer [assoc-in]]
    [io.jesi.backpack.string :refer [uuid-str?]])
  #?(:clj
     (:import (java.util UUID))))

(defmulti ->uuid
  "Coerces a value into a UUID if possible, otherwise returns nil"
  type)

(defmethod ->uuid :default [_] nil)

(defmethod ->uuid UUID [s] s)

#?(:clj (defmethod ->uuid String [s]
          (when (uuid-str? s)
            (UUID/fromString s)))

   :cljs (defmethod ->uuid js/String [s]
           (when (uuid-str? s)
             (UUID. s nil))))

(defn ->uuid-or-not [id]
  (or (->uuid id) id))

(defn assoc-changed!
  "assoc(-in) the atom when the value has changed"
  [atom & kvs]
  (let [base @atom
        updated (apply assoc-in base kvs)]
    (when (not= updated base)
      (reset! atom updated))))
