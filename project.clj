(def VERSION (.trim (slurp "VERSION")))

(defproject io.jesi/customs VERSION
  :description "Clojure(Script) cross-project testing utilities"
  :url "https://github.com/jesims/customs"
  :license {:name         "Eclipse Public License - v 1.0"
            :url          "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments     "same as Clojure"}
  :plugins [[lein-parent "0.3.8"]]
  :clean-targets ^{:protect false} [".shadow-cljs" "out" :target-path]
  :parent-project {:coords  [io.jesi/parent "3.9.0"]
                   :inherit [:plugins :managed-dependencies :deploy-repositories :dependencies :profiles :test-refresh :aliases :codox]}
  :managed-dependencies [[com.google.guava/guava "23.0"]
                         [io.jesi/backpack "5.2.1"]]
  :dependencies [[io.jesi/backpack]
                 ;CLJ
                 [org.clojure/clojure :scope "provided"]
                 [leiningen "2.9.1" :exclusions [org.slf4j/slf4j-nop]]
                 [org.clojure/tools.namespace "0.3.1"]
                 ;CLJS
                 [org.clojure/clojurescript :scope "provided"]
                 [pjstadig/humane-test-output "0.10.0"]]
  :profiles {:dev [:parent/dev {:dependencies [[lein-parent "0.3.7"]
                                               [org.slf4j/slf4j-simple "1.7.30"]
                                               [thheller/shadow-cljs]]}]})
