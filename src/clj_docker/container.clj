(ns clj-docker.container
  (:require [clj-docker.client :as client]
            [clj-docker.utils :refer :all]
            [cheshire.core :as json]
            [camel-snake-kebab :refer[->kebab-case ->CamelCase]]))

(defn create-container [cli name & {:keys [host-config]}]
  (client/post cli "/containers/create"
               (merge {:content-type :json
                       :query-params {:name name}
                       :as :json}
                      (when host-config
                        {:body (json/generate-string (map-keys ->CamelCase host-config))}))))

(defn start
  ([cli container] (start cli container {}))
  ([cli container host-config]
     (let [id (:id container)]
       (client/post cli (str "/containers/" id "/start")
                    (merge
                     {:content-type :json}
                     (when host-config
                       {:body (json/generate-string host-config)}))))))

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
