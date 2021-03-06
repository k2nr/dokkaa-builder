(ns dokkaa-builder.route
  (:require [clojure.string :as str]
            [compojure.core :refer [defroutes GET POST PUT DELETE ANY context]]
            [compojure.handler :refer [site]]
            [compojure.route :refer [resources files not-found]]
            [ring.middleware.session.cookie :as cookie]
            [environ.core :refer [env]]
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
       :body (j/encode (apps/get-app user app-name))}
      {:status 401, :body "token is invalid"})))

(defn- parse-int [s & {:keys [default]}]
  (if (or (nil? s) (empty? s))
    default
    (Integer. s)))

(defn create-app [req]
  (let [app-name (get-in req [:route-params :app])
        user  (auth/current-user req)
        image (get-in req [:params :image])
        tag   (or (get-in req [:params :tag]) "latest")
        command (get-in req [:params :command])
        port  (parse-int (get-in req [:params :port]))
        ps (parse-int (get-in req [:params :ps]) :default 1)
        port-bind-to (+ (rand-int 1000) 10000)]
    (cond
     (nil? user) {:status 401, :body "token is invalid"}
     :else (if-let [app (apps/create app-name user image
                                     :tag tag
                                     :command command
                                     :port port
                                     :ps ps)]
             {:status 200 :body (j/encode app)}
             {:status 204})
      )))

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

(defn get-access-tokens [req]
  (let [user (auth/current-user req)]
    {:status 200
     :body (j/encode (auth/get-access-tokens user))}))

(defn add-access-token [req]
  (let [user (auth/current-user req)
        token (str/replace (java.util.UUID/randomUUID) "-" "")]
    (auth/add-access-token user token {})
    {:status 200
     :body token}))

(defn delete-access-token [req]
  (let [user (auth/current-user req)
        token (get-in req [:params :token])]
    (auth/delete-access-token user token)
    {:status 200}))

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
  (GET "/access-tokens" req (get-access-tokens req))
  (POST "/access-tokens" req (add-access-token req))
  (DELETE "/access-tokens/:token" req (delete-access-token req)))

(defroutes routes
  (GET "/" req (index req))
  (GET "/status" req (status-page req))
  (context "/apps" req (friend/wrap-authorize apps-routes #{:user}))
  (context "/user" req (friend/wrap-authorize user-routes #{:user}))
  (resources "/")
  (friend/logout (ANY "/logout" request (ring.util.response/redirect "/")))
  (not-found "404 Not Found"))

(defn wrap-site [app]
  (let [store (cookie/cookie-store {:key (env :session-secret)})]
    (site app {:session {:store store}})))

(defn wrap-error [app]
  (fn [req]
    (try (app req)
      (catch Exception e
        {:status 500
         :body (j/encode {:message "Internal Server Error"})}))))

(def app (-> routes
             wrap-error
             (friend/authenticate {:allow-anon? true
                                   :workflows [(github/workflow)
                                               (dworkflows/api-token)]})
             wrap-site))
