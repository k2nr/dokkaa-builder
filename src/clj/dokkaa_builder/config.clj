(ns dokkaa-builder.config
  (:require [environ.core :refer [env]]))

(defn hipache-redis-host []
  "localhost")

(defn domain-name []
  "dokkaa.io")

(defn docker-host-url []
  (or (env :host-url) "127.0.0.1"))
