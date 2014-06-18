(ns dokkaa-builder.clusters
  (:require [dokkaa-builder.etcd :as etcd]
            [dokkaa-builder.config :as config]
            [clojurewerkz.urly.core :as urly]
            [environ.core :refer [env]]))

(defn docker-url [host & {:keys [port] :or {port 2375}}]
  (str "tcp://" host ":" port))

(defn docker-hosts []
  (try
    (mapv (fn [m] (-> m
                      (get "clientURL")
                      urly/url-like
                      urly/host-of
                      docker-url))
          (etcd/machines (str "http://" (config/docker-host-url) ":7001")))
    (catch Exception e
      (or [(env :docker-host)]
          ["tcp://127.0.0.1:2375"]))))
