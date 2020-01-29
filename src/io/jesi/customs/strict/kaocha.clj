(ns io.jesi.customs.strict.kaocha
  (:refer-clojure :exclude [remove-ns])
  (:require
    [com.rpl.specter :as sp]
    [io.jesi.backpack :as bp]
    [io.jesi.backpack.macros :refer [def-]]
    [kaocha.plugin :refer [defplugin]]))

(def- strict-ns (str 'io.jesi.customs.strict))

(defn- remove-ns [form]
  (sp/transform
    (sp/walker (bp/and-fn symbol? (bp/compr namespace #{"clojure.core" "cljs.core" strict-ns})))
    (comp symbol name)
    form))

(defplugin ::plugin
  (pre-report [{:keys [type actual expected] :as e}]
    (if (and (= :fail type)
             actual
             expected
             (not (instance? Throwable actual)))
      (-> e
          (update :actual remove-ns)
          (update :expected remove-ns))
      e)))
