(defproject dokkaa-builder "0.0.1-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [environ "0.5.0"]
                 [http-kit "2.1.18"]
                 [cheshire "5.3.1"]
                 [compojure "1.1.8"]
                 [clojurewerkz/urly "1.0.0"]
                 [com.cemerick/friend "0.2.1"
                  :exclusions [org.apache.httpcomponents/httpclient]]
                 [k2nr/docker "0.0.2-SNAPSHOT"]
                 [ring "1.2.2"]]
  :profiles {:dev {:dependencies [[midje "1.6.3"]
                                  [ring/ring-devel "1.2.2"]]}}
  :plugins [[lein-environ "0.5.0"]]
  :main dokkaa-builder.core
  :aot [dokkaa-builder.core])
