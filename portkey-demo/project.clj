(defproject portkey-test "0.1.0-SNAPSHOT"
  :description "portkey demo in Japanese"
  :url "https://github.com/k2n/portkey-demo-ja"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0-RC1"]
                 [portkey "0.1.0-SNAPSHOT"]
                 [compojure "1.6.0"]
                 [metosin/ring-http-response "0.9.0"]
                 [ring/ring-defaults "0.3.1"]
                 [ring/ring-json "0.4.0"]
                 [ring/ring-core "1.6.3"]]
  :source-paths ["src" "repl"]
  :plugins [[k2n/lein-gorilla "0.4.1"]])
