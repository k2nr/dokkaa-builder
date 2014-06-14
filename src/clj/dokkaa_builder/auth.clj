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

(defn current-identity [m]
  (:current (friend/identity m)))

(defn current-user [m]
  (-> m
      current-identity
      :user))
