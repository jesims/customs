(defproject io.jesi/aot-test "1.0.0"
  :dependencies [[org.clojure/clojure "1.10.1"]]
  :aot [gen-class-example
        secret.top.eyes-only
        secret.clearance]
  :profiles {:install {:aot [gen-class-example-2]}})
