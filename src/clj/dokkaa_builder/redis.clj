(ns dokkaa-builder.redis
  (:require [taoensso.carmine :as car]))

(def server1-conn {:pool {} :spec {:host "127.0.0.1", :port 6379}})
(defmacro wcar* [& body] `(redis/wcar server1-conn ~@body))
