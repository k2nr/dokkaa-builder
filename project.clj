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
                 [hiccup "1.0.5"]
                 [compojure "1.1.8"]
                 [com.google.guava/guava "17.0"]
                 [clojurewerkz/urly "1.0.0"
                  :exclusions [com.google.guava/guava]]
                 [com.taoensso/carmine "2.6.2"]
                 [com.cemerick/friend "0.2.1"
                  :exclusions [org.apache.httpcomponents/httpclient]]
                 [friend-oauth2 "0.1.1"
                  :exclusions [com.cemerick/friend ring clj-http cheshire org.clojure/clojure]]
                 [k2nr/docker "0.0.3-SNAPSHOT"]
                 [ring "1.3.0"]
                 [org.clojure/clojurescript "0.0-2227"]
                 [om "0.6.4"]
                 [sablono "0.2.17"]]
  :profiles {:dev {:dependencies [[ring/ring-devel "1.3.0"]
                                  [org.clojure/tools.trace "0.7.8"]]}}
  :plugins [[lein-environ "0.5.0"]
            [lein-cljsbuild "1.0.3"]
            [com.cemerick/austin "0.1.4"]]
  :source-paths ["src/clj" "src/cljs"]
  :cljsbuild {:builds [{:id "dev"
                        :source-paths ["src/cljs"]
                        :compiler {:output-to "target/classes/public/app.js"
                                   :optimizations :simple
                                   :pretty-print true
                                   :preamble ["react/react.min.js"]
                                   :externs ["react/externs/react.js"]}}
                       {:id "release"
                        :source-paths ["src/cljs"]
                        :compiler {:output-to "target/classes/public/app.js"
                                   :optimizations :advanced
                                   :pretty-print false
                                   :preamble ["react/react.min.js"]
                                   :externs ["react/externs/react.js"]}}]}
  :main dokkaa-builder.core
  :aot [dokkaa-builder.core])
