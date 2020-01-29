(ns io.jesi.customs.karma
  (:require
    [cljs.test :as ct]
    [shadow.test.karma :as karma]))

(defmethod ct/report [::karma/default :begin-test-ns] [_])

;TODO compile and export to be used in karma client args
(defn ^:export init []
  (karma/start))
