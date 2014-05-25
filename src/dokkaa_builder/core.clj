(ns dokkaa-builder.core
  (:require [environ.core :refer [env]]
            [org.httpkit.server :refer [run-server]]
            [compojure.handler :refer [site]]
            [ring.middleware.reload :as reload]
            [dokkaa-builder.route :as route]
            [dokkaa-builder.util :as util]))

(defonce server (atom nil))

(defn stop-server []
  (println "Stopping server...")
  (when-not (nil? @server)
    ;; graceful shutdown: wait 100ms for existing requests to be finished
    ;; :timeout is optional, when no timeout, stop immediately
    (@server :timeout 100)
    (reset! server nil)))

(def app (-> route/routes
             (site)))

(defn start-server
  ([] (start-server nil))
  ([port] (let [port (or port 8080)
                myapp (if (util/in-dev?)
                        (reload/wrap-reload app)
                        app)]
            (reset! server (run-server myapp {:port port}))
            (println "Listening on" port))))

(defn restart-server []
  (stop-server)
  (start-server))

(defn -main [& args]
  (start-server (Integer. (env :port)))
  (println "Starting Server..."))
