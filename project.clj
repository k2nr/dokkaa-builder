(defproject dokkaa-builder "0.0.1-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0-alpha2"]
                 [org.clojure/core.async "0.1.303.0-886421-alpha"]
                 [org.clojure/core.typed "0.2.68"]
                 [prismatic/plumbing "0.3.3"]
                 [environ "1.0.0"]
                 [http-kit "2.1.19"]
                 [cheshire "5.3.1"]
                 [clj-http "1.0.0"]
                 [hiccup "1.0.5"]
                 [ring "1.3.1"]
                 [compojure "1.1.9"]
                 [lib-noir "0.8.9"]
                 [clojurewerkz/urly "1.0.0"]
                 [com.taoensso/carmine "2.7.0"
                  :exclusions [org.clojure/clojure]]
                 [com.cemerick/friend "0.2.1"
                  :exclusions [org.apache.httpcomponents/httpclient]]
                 [friend-oauth2 "0.1.1"]
                 [k2nr/docker "0.0.3-SNAPSHOT"]
                 [ring/ring-defaults "0.1.2"]
                 [org.clojure/clojurescript "0.0-2342"]
                 [om "0.7.3"]
                 [sablono "0.2.22"]
                 [cljs-http "0.1.16"]
                 [secretary "1.2.1"]]
  :profiles {:dev {:dependencies [[ring/ring-devel "1.3.1"]
                                  [org.clojure/tools.trace "0.7.8"]]}}
  :repl-options {:init-ns dokkaa-builder.core}
  :plugins [[lein-environ "1.0.0"]
            [lein-cljsbuild "1.0.3"]
            [com.cemerick/austin "0.1.5"]]
  :source-paths ["src/clj" "src/cljs"]
  :cljsbuild {:builds [{:id "dev"
                        :source-paths ["src/cljs"]
                        :compiler {:output-to "target/classes/public/app.js"
                                   :optimizations :whitespace
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
