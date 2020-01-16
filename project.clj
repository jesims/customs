(def VERSION (.trim (slurp "VERSION")))

(defproject io.jesi/customs VERSION
  :description "Clojure(Script) cross-project testing utilities"
  :url "https://github.com/jesims/backpack"
  :license {:name         "Eclipse Public License - v 1.0"
            :url          "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments     "same as Clojure"}
  :plugins [[lein-parent "0.3.7"]]
  :clean-targets [:target-path :compile-path "out"]
  :parent-project {:coords  [io.jesi/parent "3.0.2"]
                   :inherit [:plugins :managed-dependencies :deploy-repositories :dependencies :profiles :test-refresh :aliases :codox]}
  :managed-dependencies [[com.google.guava/guava "23.0"]
                         [io.jesi/customs ~(str VERSION "-SNAPSHOT")]]
  :dependencies [[io.jesi/backpack "4.2.1"]                 ;TODO use managed version (at least 5.0)
                 ;CLJ
                 [org.clojure/clojure :scope "provided"]
                 [leiningen "2.9.1" :exclusions [org.slf4j/slf4j-nop]]
                 [org.clojure/tools.namespace "0.3.1"]
                 ;CLJS
                 [thheller/shadow-cljs :scope "provided"]
                 [org.clojure/clojurescript :scope "provided"]
                 [pjstadig/humane-test-output "0.10.0"]]
  :profiles {:dev [:parent/dev {:dependencies [[lein-parent "0.3.7"]]}]})
