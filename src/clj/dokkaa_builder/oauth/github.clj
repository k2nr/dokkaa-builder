(ns dokkaa-builder.oauth.github
  (:require [cemerick.friend :as friend]
            [friend-oauth2.workflow :as oauth2]
            [friend-oauth2.util :refer [format-config-uri get-access-token-from-params]]
            [clj-http.client :as http]
            [cheshire.core :as j]
            [dokkaa-builder.redis :refer [wcar*]]
            [dokkaa-builder.auth :as auth]
            [taoensso.carmine :as redis]
            [ring.util.request :as request]))

(def client-config
  {:client-id         "831a1e47f620227fd97c"
   :client-secret     "24de8c80943503f528ba9669b9914adb49ee66fd"
   :callback {:domain "http://dokkaa.io:8080" :path "/users/oauth/callback"}})

(def uri-config
  {:authentication-uri {:url "https://github.com/login/oauth/authorize"
                        :query {:client_id (:client-id client-config)
                                :response_type "code"
                                :redirect_uri (format-config-uri client-config)
                                :scope []}}

   :access-token-uri {:url "https://github.com/login/oauth/access_token"
                      :query {:client_id (:client-id client-config)
                              :client_secret (:client-secret client-config)
                              :grant_type "authorization_code"
                              :redirect_uri (format-config-uri client-config)}}})

(defn get-github-user [token]
  (let [url (str "https://api.github.com/user?access_token=" token)]
    (j/decode (:body (http/get url {:accept :json})))))

(defn set-user! [user]
  (let [login-name (user "login")
        user-id (wcar* (redis/get (str "github:login:" login-name)))]
    (if-not user-id
      (let [user (auth/add-new-user :username login-name)]
        (wcar* (redis/set (str "github:login:" login-name) (:id user)))
        user)
      (auth/get-user user-id))))

(defn- default-credential-fn [token]
  (let [token (:access-token token)
        github-user (get-github-user token)
        user (set-user! github-user)]
    {:identity {:user user}
     :roles #{:user}}))

(defn workflow [& {:keys [credential-fn]}]
  (oauth2/workflow
   {:client-config client-config
    :uri-config    uri-config
    :access-token-parsefn get-access-token-from-params
    :login-uri "/oauth/github"
    :credential-fn #(merge (default-credential-fn %)
                           (when credential-fn
                             (credential-fn %)))}))
