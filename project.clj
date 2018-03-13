(defproject io.jesi/backpack "0.0.1"
  :description "Clojure(Script) cross project utilities"
  :license "Unlicensed"
  :url "https://github.com/jesims/backpack"
  :min-lein-version "2.7.0"
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/clojurescript "1.9.946"]
                 [com.rpl/specter "1.1.0"]
                 [com.lucasbradstreet/cljs-uuid-utils "1.0.2"]]
  :exclusions [org.clojure/clojure
               org.clojure/clojurescript]
  :plugins [[lein-cljsbuild "1.1.7"]
            [s3-wagon-private "1.3.1"]]
  :source-paths ["src"]
  :test-paths ["test/cljc"]
  :clean-targets ^{:protect false} ["target"]
  :test-refresh {:quiet        true
                 :with-repl    true
                 :changes-only true}
  :profiles {:dev
             {:plugins      [
                             [lein-ancient "0.6.14"]
                             [lein-doo "0.1.8"]]
              :dependencies [[circleci/circleci.test "0.4.1"]
                             [pjstadig/humane-test-output "0.8.3"]]
              :injections   [(require 'pjstadig.humane-test-output)
                             (pjstadig.humane-test-output/activate!)]}}
  :cljsbuild {:builds
              {:test {:source-paths ["src" "test/cljc"]
                      :compiler     {:main           io.jesi.backpack.runner
                                     :output-dir     "target/test"
                                     :output-to      "target/test/test.js"
                                     :optimizations  :none
                                     :pretty-print   true
                                     :process-shim   false
                                     :parallel-build true
                                     :target         :nodejs}}}}
  :doo {:build "test" :alias {:default [:node]}}
  :aliases {"test-cljs" ["doo" "once"]}
  :release-tasks [["vcs" "assert-committed"]
                  ["deploy"]]
  :repositories {"releases"  {:url           "s3p://artifacts.jesi.io/releases/"
                              :no-auth       true
                              :sign-releases false}
                 "snapshots" {:url           "s3p://artifacts.jesi.io/snapshots/"
                              :no-auth       true
                              :sign-releases false}})
