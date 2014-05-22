(ns clj-docker.core
  (:require [clj-docker.client :as client]
            [clj-docker.container :as container]
            [clj-docker.utils :as utils]
            [clj-docker.image :as image]
            [slingshot.slingshot :refer [try+]]))

(def make-client clj-docker.client/make-client)

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
                   (container/create-container-from-config cli host-config :name name)
                   (catch #(= (:type %) ::clj-docker.client/not-found) _
                     (image/create-image cli image
                                         :tag tag
                                         :repo repo
                                         :registry registry)
                     (container/create-container-from-config cli host-config :name name)))]

    (container/start cli (:id container)
                     :binds lxc-conf
                     :lxc-conf lxc-conf
                     :port-bindigs port-bindings
                     :publish-all-ports port-bindings
                     :privileged privileged)))

;; examples
(comment
  (def cli(clj-docker.client/make-client))
  (version cli)
  (clj-docker.core/run cli "ubuntu" :tag "14.04" :cmd ["ls"]))
