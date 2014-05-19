(ns clj-docker.core
  (:require [clj-docker.client :as client]
            [clj-docker.container :as container]
            [clj-docker.image :as image]))

(defn version [cli]
  (client/get cli "/version" {:as :json}))

(defn- create!
  "create the container. if the container not found, try pull and retry create"
  [cli name & {:keys [host-config tag repo registry]}]
  (try+
   (container/create-container cli name :host-config host-config)
   (catch #(= (:type %) :client/not-found) _
     (image/create-image cli name :tag tag :repo :registry registry)
     (container/create-container cli name :host-config host-config))))

(defn run [cli name & {:keys [host-config tag repo registry]}]
  (let [container (create! cli name
                           :host-config host-config
                           :tag tag
                           :repo repo
                           :registry registry)]
    (container/start cli container host-config)))
