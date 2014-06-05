(defproject dokkaa-builder "0.0.1-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [environ "0.5.0"]
                 [http-kit "2.1.18"]
                 [cheshire "5.3.1"]
                 [clj-http "0.9.2"]
                 [compojure "1.1.8"]
                 [clojurewerkz/urly "1.0.0"]
                 [com.taoensso/carmine "2.6.2"]
                 [com.cemerick/friend "0.2.1"
                  :exclusions [org.apache.httpcomponents/httpclient]]
                 [friend-oauth2 "0.1.1"
                  :exclusions [com.cemerick/friend ring clj-http cheshire org.clojure/clojure]]
                 [k2nr/docker "0.0.3-SNAPSHOT"]
                 [ring "1.3.0"]
                 [org.clojure/clojurescript "0.0-2173"]
                 [om "0.6.4"]]
  :profiles {:dev {:dependencies [[ring/ring-devel "1.3.0"]
                                  [org.clojure/tools.trace "0.7.8"]]}}
  :plugins [[lein-environ "0.5.0"]
            [lein-cljsbuild "1.0.3"]]
  :source-paths ["src/clj"]
  :cljsbuild {:builds [{:source-paths ["src/cljs"]
                        :compiler {:output-to "resources/public/js/main.js"}
                        :optimizations :whitespace
                        :prety-print true}]}
  :main dokkaa-builder.core
  :aot [dokkaa-builder.core])
