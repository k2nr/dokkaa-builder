(ns dokkaa-builder.route
  (:require [compojure.core :refer [defroutes GET POST PUT DELETE ANY context]]
            [compojure.handler :refer [site]]
            [compojure.route :refer [resources files not-found]]
            [cheshire.core :as j]
            [cemerick.friend :as friend]
            [dokkaa-builder.apps :as apps]
            [dokkaa-builder.auth :as auth]
            [dokkaa-builder.pages :as pages]
            [dokkaa-builder.workflows :as dworkflows]
            [dokkaa-builder.oauth.github :as github]))

(defn get-apps [req]
  (let [app-name (get-in req [:route-params :app])
        user  (auth/current-user req)]
    (if user
      {:status 200
       :body (j/encode (apps/apps user))}
      {:status 401, :body "token is invalid"})))

(defn get-app [req]
  (let [app-name (get-in req [:route-params :app])
        user  (auth/current-user req)]
    (if user
      {:status 200
       :body (j/encode (apps/app user app-name))}
      {:status 401, :body "token is invalid"})))

(defn create-app [req]
  (let [app-name (get-in req [:route-params :app])
        user  (auth/current-user req)
        image (get-in req [:params :image])
        tag   (or (get-in req [:params :tag]) "latest")
        command (get-in req [:params :command])
        port  (get-in req [:params :port])
        ps (Integer. (or (get-in req [:params :ps]) 1))
        port-bind-to (+ (rand-int 1000) 10000)]
    (if user
      {:status 200
       :body (j/encode (apps/create app-name user image
                                    :tag tag
                                    :command command
                                    :port port
                                    :ps ps))}
      {:status 401, :body "token is invalid"})))

(defn update-app [req]
  )

(defn delete-app [req]
  (let [app-name (get-in req [:route-params :app])
        user (auth/current-user req)]
    (if user
      {:status 200
       :body (j/encode (apps/delete app-name user))}
      {:status 401, :body "token is invalid"})))

(defn logs [req]
  (let [app-name (get-in req [:route-params :app])
        user (auth/current-user req)]
    (if user
      (apps/logs app-name user)
      {:status 401, :body "token is invalid"})))

(defn index [req]
  (if (friend/authorized? [:user] req)
    (pages/index)
    "<a href=\"/oauth/github\">Login By GitHub</a>"))

(defn status-page [request]
  (let [count (:count (:session request) 0)
        session (assoc (:session request) :count (inc count))]
    (-> (ring.util.response/response
         (str "<p>We've hit the session page " (:count session)
              " times.</p><p>The current session: " session "</p>"))
        (assoc :session session))))

(defroutes apps-routes
  (GET    "/" req (get-apps req))
  (GET    "/:app" req (get-app req))
  (GET    "/:app/logs" req (logs req))
  (POST   "/:app" req (create-app req))
  (PUT    "/:app" req (update-app req))
  (DELETE "/:app" req (delete-app req)))

(defroutes user-routes
  )

(defroutes routes
  (GET "/" req (index req))
  (GET "/status" req (status-page req))
  (context "/apps" req (friend/wrap-authorize apps-routes #{:user}))
  (context "/user" req (friend/wrap-authorize users-routes #{:user}))
  (resources "/")
  (friend/logout (ANY "/logout" request (ring.util.response/redirect "/")))
  (not-found "404 Not Found"))

(def app (-> routes
             (friend/authenticate {:allow-anon? true
                                   :workflows [(github/workflow)
                                               (dworkflows/api-token)]})
             site))
