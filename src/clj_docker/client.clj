(ns clj-docker.client
  (:require [clj-http.client :as http]))

(defprotocol DockerClient
  (url [this path]))

(defprotocol RESTClient
  (get    [this path opts])
  (delete [this path opts])
  (post   [this path opts]))

(defn- make-opts [opts]
  (merge {:as :stream} opts))

(defrecord Client [host]
  DockerClient
  (url [this path] (str "http://" host path))
  RESTClient
  (get    [this path opts] (http/get    (url this path) (make-opts opts)))
  (delete [this path opts] (http/delete (url this path) (make-opts opts)))
  (post   [this path opts] (http/post   (url this path) (make-opts opts))))

(defn make-client
  ([] (make-client "localhost:4243"))
  ([host] (Client. host)))
