(ns dokkaa-builder.auth
  (:require [cemerick.friend.credentials :as creds]))

;; these atoms are in-memory databases just for develoment
(def users (atom {1 {:id 1
                     :username "john"
                     :password (creds/hash-bcrypt "password")}
                  2 {:id 2
                     :username "mike"
                     :password (creds/hash-bcrypt "passw0rd")}}))
(def tokens (atom {"ABCDEFGHIJKLMN" 2}))

(defn token->user-id [token]
  (get @tokens token))

(defn user-id->user [id]
  (get @users id))

(defn token->user [token]
  (->> token
       token->user-id
       user-id->user))

(defn user-name->user [user-name]
  (get-in
   (filterv #(= (:username (second %)) user-name) @users)
   [0 1]))
