(ns io.jesi.customs.kaocha
  (:require
    [io.jesi.customs.util :as util])
  (:import
    (java.io ByteArrayOutputStream)))

(defn reset-output []
  (when (and (util/provided? 'kaocha.plugin.capture-output)
             ;deref var and atom
             (seq @@(resolve 'kaocha.plugin.capture-output/active-buffers)))
    (when-let [^ByteArrayOutputStream test-buffer @(resolve 'kaocha.plugin.capture-output/*test-buffer*)]
      (-> test-buffer .reset))))
