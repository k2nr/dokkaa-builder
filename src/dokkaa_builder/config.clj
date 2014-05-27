(ns dokkaa-builder.config
  (:require [environ.core :refer [env]]))

(defn hipache-redis-host []
  "localhost")

(defn domain-name []
  "dokkaa.io")
