(ns dokkaa-builder.auth
  (:require [cemerick.friend :as friend]
            [dokkaa-builder.redis :refer [wcar*]]
            [taoensso.carmine :as redis]
            [cemerick.friend.credentials :as creds]))

(defn new-user-id []
  (wcar* (redis/incr "auto-user-id")))

(defn add-new-user [& {:keys [username]}]
  (let [id (new-user-id)
        user {:id id, :username username}]
    (wcar* (redis/set
            (str "users:" id)
            user))
    user))

(defn get-user [id]
  (wcar* (redis/get (str "users:" id))))

(defn get-access-tokens [user]
  (let [keys (second (wcar* (redis/scan 0 "MATCH"
                                        (str "access-tokens:" (:id user) ":*"))))]
    (map #(->> %
              (re-seq #"access-tokens:[0-9]+:(.*)$")
              first
              second)
         keys)))

(defn add-access-token [user token permissions]
  (wcar* (redis/set (str "access-tokens:" (:id user) ":" token)
                    {:permissions permissions})))

(defn delete-access-token [user token]
  (wcar* (redis/del (str "access-tokens:" (:id user) ":" token))))

(defn current-identity [m]
  (:current (friend/identity m)))

(defn current-user [m]
  (-> m
      current-identity
      :user))
