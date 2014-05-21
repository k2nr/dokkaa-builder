(ns clj-docker.container
  (:require [clj-docker.client :as client]
            [clj-docker.utils :refer :all]
            [cheshire.core :as json]
            [camel-snake-kebab :refer[->kebab-case ->CamelCase]]))

(defn ->host-config [config]
  (map-keys ->CamelCase config))

(defn create-container-from-config [cli host-config & {:keys [name]}]
  (map-keys ->kebab-case
            (client/post cli "/containers/create"
                         {:content-type :json
                          :query-params {:name name}
                          :as :json
                          :body (json/generate-string
                                 (map-keys ->CamelCase host-config))})))

(defn create-container [cli image & {:keys [; host config
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
                                            ; query parameters
                                            name
                                           ] :as config}]
  (let [host-config (-> config
                        (assoc :image image)
                        (dissoc :name)
                        (->host-config))]
    (create-container-from-config cli host-config :name name)))

(defn start
  ([cli container & {:keys [host-config stream]}]
     (let [id (:id container)]
       (client/post cli (str "/containers/" id "/start")
                    (merge
                     {:content-type :json
                      :as (if stream :stream :json)}
                     (when host-config
                       {:body (if host-config
                                (json/generate-string host-config)
                                "{}")}))))))

(defn attach [cli container & {:keys [logs stream stdin stdout stderr]}]
  (let [id (:id container)]
    (client/post cli (str "/containers/" id "/attach")
                 {:query-params {:logs logs
                                 :stream stream
                                 :stdin  stdin
                                 :stdout stdout
                                 :stderr stderr}
                  :as :stream})))

(defn logs [cli container & {:keys [follow stdout stderr timestamps]}]
  (let [id (:id container)]
    (client/get cli (str "/containers/" id "/logs")
                {:query-params {:follow follow
                                :stdout stdout
                                :stderr stderr
                                :timestamps timestamps}
                 :as :stream})))

(defn list-containers [cli & {:keys [all limit since before  size]}]
  (client/get cli "/containers/json"
              {:query-params {:all all
                              :limit limit
                              :since since
                              :before before
                              :size size}
               :as :json}))

(defn inspect [cli container]
  (let [id (:id container)]
    (client/get cli (str "/containers/" id "/json")
                {:as :json})))

(defn stop [cli container & {:keys [time]}]
  (let [id (:id container)]
    (client/post cli (str "/containers/" id "/stop")
                 {:query-params {:t time}})))

(defn kill [cli container & {:keys [signal]}]
  (let [id (:id container)]
    (client/post cli (str "/containers/" id "/kill")
                 {:query-params {:signal signal}})))

(defn remove [cli container & {:keys [remove-volumes force]}]
  (let [id (:id container)]
    (client/delete cli (str "/containers" id)
                   {:query-params {:v remove-volumes
                                   :force force}})))
