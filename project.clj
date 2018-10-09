(def VERSION (.trim (slurp "VERSION")))

(defproject io.jesi/backpack VERSION
  :description "Clojure(Script) cross-project utilities"
  :url "https://github.com/jesims/backpack"
  :license {:name         "Eclipse Public License - v 1.0"
            :url          "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments     "same as Clojure"}
  :plugins [[lein-parent "0.3.5"]]
  :parent-project {:coords  [io.jesi/parent "0.0.1"]
                   :inherit [:plugins :managed-dependencies :deploy-repositories :dependencies :exclusions [:profiles :dev] :test-refresh]}
  :dependencies [[org.clojure/clojurescript]
                 [com.rpl/specter]
                 [thheller/shadow-cljs]
                 [com.lucasbradstreet/cljs-uuid-utils "1.0.2"]
                 [medley "1.0.0"]
                 [org.clojure/tools.namespace "0.2.11"]
                 [pjstadig/humane-test-output "0.8.3"]]
  :clean-targets ^{:protect false} ["target"]
  :aot [io.jesi.backpack.random]
  :release-tasks [["deploy"]])
