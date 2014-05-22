(ns clj-docker.core
  (:require [clj-docker.client :as client]
            [clj-docker.container :as container]
            [clj-docker.utils :as utils]
            [clj-docker.image :as image]
            [slingshot.slingshot :refer [try+]]
            [camel-snake-kebab :refer[->kebab-case ->CamelCase]]))

(def make-client client/make-client)

(defn ping [cli]
  (client/get cli "/_ping" {}))

(defn version [cli]
  (client/get cli "/version" {:as :json}))

(defn run [cli image & {:keys [; create host config
                               hostname
                               domainname
                               exposed-ports
                               user
                               tty
                               open-stdin
                               stdin-once
                               memory
                               attach-stdin
                               attach-stdout
                               attach-stderr
                               env
                               cmd
                               dns
                               volumes
                               volumes-from
                               network-disabled
                               entrypoint
                               cpu-shares
                               working-dir
                               memory-swap
                               ; start host config
                               binds
                               lxc-conf
                               port-bindings
                               publish-all-ports
                               privileged
                               ; query parameters
                               name
                               tag
                               repo
                               registry
                               ] :as config}]
  (let [host-config (-> config
                        (assoc :image (str image (when tag (str ":" tag))))
                        (dissoc :name :tag :repo :registry))
        container (try+
                   (container/create-from-config cli host-config :name name)
                   (catch #(= (:type %) ::clj-docker.client/not-found) _
                     (image/create-image cli image
                                         :tag tag
                                         :repo repo
                                         :registry registry)
                     (container/create-from-config cli host-config :name name)))]

    (container/start cli (:id container)
                     :binds lxc-conf
                     :lxc-conf lxc-conf
                     :port-bindigs port-bindings
                     :publish-all-ports port-bindings
                     :privileged privileged)
    (:id container)))

(defn commit [cli container & {:keys [repo tag message author]}]
  (utils/map-keys ->kebab-case
            (client/post cli "/commit"
                        {:query-params {:container container
                                        :repo repo
                                        :tag tag
                                        :m message
                                        :author author}
                         :as :json})))

;; examples
(comment
  (def cli(clj-docker.client/make-client))
  (version cli)
  (clj-docker.core/run cli "ubuntu" :tag "14.04" :cmd ["ls"]))
