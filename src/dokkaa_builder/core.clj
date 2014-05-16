(ns dokkaa-builder.core
  (:use [environ.core :only [env]]
        [org.httpkit.server :only [run-server]]
        [compojure.core :only [defroutes GET POST DELETE ANY context]]
        [compojure.handler :only [site]]
        [compojure.route :only [files not-found]]
        [ring.middleware.reload :as reload]))

(defn build [params body]
  (str params "," (slurp body)))

(defn ping [req]
  "hello")

(defroutes routes
  (POST "/" {params :params, body :body} (build params body))
  (GET  "/"  [] ping))

(defonce server (atom nil))

(defn stop-server []
  (println "Stopping server...")
  (when-not (nil? @server)
    ;; graceful shutdown: wait 100ms for existing requests to be finished
    ;; :timeout is optional, when no timeout, stop immediately
    (@server :timeout 100)
    (reset! server nil)))

(defn stage []
  (or (env :stage) "development"))

(defn in-dev? []
  (= (stage) "development"))

(def app (-> routes
             (site)))

(defn start-server
  ([] (start-server nil))
  ([port] (let [port (or port 8080)
                myapp (if (in-dev?)
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
