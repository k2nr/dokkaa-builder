(ns dokkaa-builder.workflows
  (:require [cemerick.friend :as friend]
            [cheshire.core :as j]
            [dokkaa-builder.redis :refer [wcar*]]
            [dokkaa-builder.auth :as auth]
            [ring.util.request :as request]))


(defn api-token []
  (fn [req]
    (when-let [token (get-in req [:params :api-token])]
      (when-let [user (auth/token->user token)]
        (vary-meta {:identity {:user user} :roles #{:user}}
                   merge {::friend/workflow :api-token
                          ::friend/redirect-on-auth? false
                          :type ::friend/auth})))))
