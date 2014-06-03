(ns dokkaa-builder.route
  (:require [compojure.core :refer [defroutes GET POST PUT DELETE ANY context]]
            [compojure.route :refer [files not-found]]
            [dokkaa-builder.apps :as apps]
            [dokkaa-builder.auth :as auth]
            [friend-oauth2.workflow :as oauth2]
            [dokkaa-builder.middleware.auth :as mauth]))

(defn create-app [req]
  (let [app-name (get-in req [:route-params :app])
        token (get-in req [:params :token])
        user  (auth/token->user token)
        image (get-in req [:params :image])
        tag   (or (get-in req [:params :tag]) "latest")
        command (get-in req [:params :command])
        port  (get-in req [:params :port])
        port-bind-to (+ (rand-int 1000) 10000)]
    (if user
      (apps/create app-name user image
                   :tag tag
                   :command command
                   :port port)
      {:status 401, :body "token is invalid"})))

(defn update-app [req]
  )

(defn delete-app [req]
  (let [app-name (get-in req [:route-params :app])
        token (get-in req [:params :token])
        user (auth/token->user token)]
    (if user
      (apps/delete app-name user)
      {:status 401, :body "token is invalid"})))

(defn logs [req]
  (let [app-name (get-in req [:route-params :app])
        token (get-in req [:params :token])
        user (auth/token->user token)]
    (if user
      (apps/logs app-name user)
      {:status 401, :body "token is invalid"})))

(defn ping [req]
  {:status 200})

(defn create-user [req]
  )

(defroutes apps-routes
  (GET    "/logs" req (logs req))
  (POST   "/" req (create-app req))
  (PUT    "/" req (update-app req))
  (DELETE "/" req (delete-app req)))

(defroutes users-routes
  (POST "/" req (create-user req)))

(defroutes routes
  (GET  "/_ping"  [] ping)
  (context "/users" req (-> users-routes
                            mauth/friend-middleware))
  (context "/apps/:app" req apps-routes))
