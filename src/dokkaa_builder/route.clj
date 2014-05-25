(ns dokkaa-builder.route
  (:require [compojure.core :refer [defroutes GET POST PUT DELETE ANY context]]
            [compojure.route :refer [files not-found]]
            [k2nr.docker.core :as docker]
            [dokkaa-builder.apps :as apps]))

(defn create-app [req]
  (let [app-name (get-in req [:route-params :app])
        image (get-in req [:params :image])
        tag   (or (get-in req [:params :tag]) "latest")
        command (get-in req [:params :command])
        port  (get-in req [:params :port])
        port-bind-to (+ (rand-int 10000) 40000)]
    (println "port-bind-to: " port-bind-to)
    (apps/create app-name
                 image
                 :tag tag
                 :command command
                 :port-bindings [(str port-bind-to ":" port)])))

(defn update-app [req]
  )

(defn delete-app [req]
  )

(defn ping [req]
  {:status 200})

(defroutes apps-routes
  (POST   "/" req (create-app req))
  (PUT    "/" req (update-app req))
  (DELETE "/" req (delete-app req)))

(defroutes routes
  (GET  "/_ping"  [] ping)
  (context "/apps/:app" req apps-routes))
