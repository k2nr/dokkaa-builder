(ns dokkaa-builder.api
  (:require [cljs-http.client :as http]))

(defn get-apps [host & {:keys [token]}]
  (http/get (str "http://" host "/apps") {:query-params {:token token}}))

(defn get-access-tokens [host & {:keys [token]}]
  (http/get (str "http://" host "/user/access-tokens") {:query-params {:token token}}))

(defn add-access-token [host & {:keys [token]}]
  (http/post (str "http://" host "/user/access-tokens")))

(defn delete-access-token [host access-token & {:keys [token]}]
  (http/delete (str "http://" host "/user/access-tokens/" access-token)))

(defn delete-app [host app-name & {:keys [token]}]
  (http/delete (str "http://" host "/apps/" app-name)))

(defn create-app [host app-name opts & {:keys [token]}]
  (http/post (str "http://" host "/apps/" app-name)
             {:query-params opts}))
