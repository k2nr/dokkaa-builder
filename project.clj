(defproject dokkaa-builder "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [docker "0.2.0"]
                 [environ "0.5.0"]
                 [circleci/clj-yaml "0.5.2"]
                 [http-kit "2.1.18"]
                 [compojure "1.1.8"]
                 [clojurewerkz/urly "1.0.0"]
                 [ring/ring-devel "1.2.2"]
                 [ring "1.2.2"]
                 [clj-http "0.9.1"]
                 [org.apache.commons/commons-compress "1.8.1"]]
  :profiles {:dev {:dependencies [[midje "1.6.3"]]}}
  :plugins [[lein-environ "0.5.0"]]
  :main dokkaa-builder.core)
