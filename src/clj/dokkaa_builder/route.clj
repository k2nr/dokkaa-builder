(ns dokkaa-builder.route
  (:require [compojure.core :refer [defroutes GET POST PUT DELETE ANY context]]
            [compojure.handler :refer [site]]
            [compojure.route :refer [resources files not-found]]
            [dokkaa-builder.apps :as apps]
            [dokkaa-builder.auth :as auth]
            [dokkaa-builder.pages :as pages]
            [clj-http.client :as http]
            [cheshire.core :as j]
            [cemerick.friend :as friend]
            [dokkaa-builder.oauth.github :as github]))

(declare render-status-page)
(declare render-repos-page)
(declare get-github-repos)

(defn render-status-page [request]
  (let [count (:count (:session request) 0)
        session (assoc (:session request) :count (inc count))]
    (-> (ring.util.response/response
           (str "<p>We've hit the session page " (:count session)
                " times.</p><p>The current session: " session "</p>"))
         (assoc :session session))))

(defn render-repos-page
  "Shows a list of the current users github repositories by calling the github api
   with the OAuth2 access token that the friend authentication has retrieved."
  [request]
  (prn request)
  (let [access-token (get-in request [:session :cemerick.friend/identity :current :access-token])
        repos-response (get-github-repos access-token)]
    (str (vec (map :name repos-response)))))

(defn get-github-repos
  "Github API call for the current authenticated users repository list."
  [access-token]
  (let [url (str "https://api.github.com/user/repos?access_token=" access-token)
        response (http/get url {:accept :json})
        repos (j/parse-string (:body response) true)]
    repos))

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

(defroutes apps-routes
  (GET    "/logs" req (logs req))
  (POST   "/" req (create-app req))
  (PUT    "/" req (update-app req))
  (DELETE "/" req (delete-app req)))

(defroutes users-routes
  (GET "/" request "<a href=\"/users/repos\">My Github Repositories</a><br><a href=\"/users/status\">Status</a>")
  (GET "/status" request (render-status-page request))
  (GET "/repos"  request (friend/authorize #{:user} (render-repos-page request)))
  (friend/logout (ANY "/logout" request (ring.util.response/redirect "/"))))

(defroutes routes
  (GET "/" [] (pages/index))
  (GET "/_ping"  [] ping)
  (context "/users" req users-routes)
  (context "/apps/:app" req apps-routes)
  (resources "/")
  (not-found "404 Not Found"))

(def app (-> routes
             (github/authenticate
              :credential-fn (fn [token]))
             site))
