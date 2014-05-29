(ns dokkaa-builder.apps-router
  (:require [taoensso.carmine :as redis]))

(def server1-conn {:pool {} :spec {:host "127.0.0.1", :port 6379}})
(defmacro wcar* [& body] `(redis/wcar server1-conn ~@body))

(defn add-upstream
  ([domain upstream]
     (wcar* (redis/rpush (str "frontend:" domain) upstream)))
  ([domain upstream & rest]
     (wcar* (apply redis/rpush domain upstream rest))))

(defn add-domain [domain & upstreams]
  (wcar* (redis/rpush (str "frontend:" domain) domain)
         (when upstreams (apply add-upstream domain upstreams))))

(defn delete-upstream [domain upstream]
  (wcar*
   (redis/lrem (str "frontend:" domain) 1 upstream)))

(defn delete-domain [domain]
  (wcar* (redis/del (str "frontend:" domain))))

(defn domains [& {:keys [upstreams]}]
  (let [ds (wcar* (redis/keys "*"))]
    (if upstreams
      (mapv #(hash-map % (wcar* (redis/lrange % 0 -1))) ds)
      ds)))
