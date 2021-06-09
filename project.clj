(def VERSION (.trim (slurp "VERSION")))

(defproject io.jesi/customs VERSION
  :description "Clojure(Script) cross-project testing utilities"
  :url "https://github.com/jesims/customs"
  :license {:name         "Eclipse Public License - v 1.0"
            :url          "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments     "same as Clojure"}
  :plugins [[lein-parent/lein-parent "0.3.8"]]
  :clean-targets ^{:protect false} [".shadow-cljs" "out" :target-path]
  :parent-project {:coords  [io.jesi/parent "4.11.0"]
                   :inherit [:plugins :managed-dependencies :deploy-repositories :dependencies :profiles :test-refresh :aliases :codox]}
  :managed-dependencies [[io.jesi/backpack "7.1.0"]]
  :dependencies [[io.jesi/backpack]
                 ;CLJ
                 [org.clojure/clojure :scope "provided"]
                 [leiningen/leiningen "2.9.6" :scope "provided" :exclusions [org.slf4j/slf4j-nop]]
                 [org.clojure/tools.namespace "1.1.0"]
                 ;CLJS
                 [org.clojure/clojurescript :scope "provided"]
                 [pjstadig/humane-test-output "0.11.0"]]
  :profiles {:dev [:parent/dev {:dependencies [[lein-parent/lein-parent "0.3.8"]
                                               [org.slf4j/slf4j-simple "1.7.30"]
                                               [thheller/shadow-cljs :scope "provided"]]}]})
