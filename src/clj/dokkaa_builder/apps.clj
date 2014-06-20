(ns dokkaa-builder.apps
  (:require [clojure.string :as str]
            [k2nr.docker.core :as docker]
            [k2nr.docker.container :as container]
            [dokkaa-builder.apps-router :as router]
            [dokkaa-builder.config :as config]
            [dokkaa-builder.clusters :as clusters]
            [dokkaa-builder.redis :refer [wcar*]]
            [clojurewerkz.urly.core :as urly]
            [taoensso.carmine :as redis]
            [slingshot.slingshot :refer [try+]]
            ))

(declare delete-instances)

(defn- rkey [& args]
  "construct redis key"
  (str/join ":" args))

(defn apps [user]
  (let [keys (wcar* (redis/keys (rkey "users" (:id user) "apps" "*")))]
    (mapv #(wcar* (redis/get %)) keys)))

(defn app [user app-name]
  (wcar* (redis/get (rkey "users" (:id user) "apps" app-name))))

(defn set-app [user app-name app]
  (wcar* (redis/set (rkey "users" (:id user) "apps" app-name) app)))

(defn delete-app [user app-name]
  (wcar* (redis/del (rkey "users" (:id user) "apps" app-name))))

(defn- pick-backends
  "Randomly choose docker backend"
  [n]
  (mapv (fn [host] {:client (docker/make-client host)
                    :port   (router/generate-host-port (urly/host-of host))})
        (->> (clusters/docker-hosts)
             shuffle
             (take n))))

(defn- default-frontend-url [app-name]
  (str app-name "." (config/domain-name)))

(defn- upstream-url [cli port]
  (let [host (urly/url-like (str "http://" (.host cli)))
        url (str "http://" (urly/host-of host) ":" port)]
    url))

(defn instances [user app-name]
  (:instances (app user app-name)))

(defn future-create-instance [cli host-port image & {:keys [port tag command]}]
  (future
    (let [id (docker/run cli image
               :tag tag
               :cmd command
               :port-bindings {host-port port})
          upstream (upstream-url cli host-port)]
      {:id id, :docker-host (.host cli), :host upstream})))

(defn create [app-name user image & {:keys [tag command port ps]}]
  (let [ps (or ps 1)
        backends (pick-backends ps)
        front-url (default-frontend-url app-name)
        old-isntances (instances user app-name)
        app {:name    app-name
             :status  :creating
             :image   image
             :tag     tag
             :ps      (count backends)
             :user-id (:id user)}]
    (set-app user app-name app)
    (future
      (try+ (let [futures (doall (for [{cli :client host-port :port} backends]
                                   (future-create-instance cli host-port image
                                                           :port port
                                                           :tag tag
                                                           :command command)))
                  new-instances (mapv deref futures)]
              (when old-isntances (router/delete-domain front-url))
              (apply router/add-domain front-url (map :host new-instances))
              (set-app user app-name (merge app {:status :running
                                                 :front-urls [front-url]
                                                 :instances  new-instances}))
              (when old-isntances (delete-instances old-isntances)))
            (catch Object _
              (set-app user app-name (merge app {:status :failed})))))
    app))

(defn update [req]
  )

(defn delete [app-name user]
  (let [is (instances user app-name)]
    (delete-instances is)
    (delete-app user app-name)
    (router/delete-domain (default-frontend-url app-name))
    nil))

(defn delete-instances [instances]
  (doseq [i instances]
    (let [{:keys [docker-host id]} i
          cli (docker/make-client docker-host)]
      (container/stop cli id)
      (container/remove cli id))))

(defn logs [app-name user]
  (let [{cli :client} (first (pick-backends 1))
        resp (container/logs cli (-> app-name #(instances user) first :id) :stdout true :stderr true)]
    (map #(str (name (:stream-type %))
               ": "
               (:body %)) resp)))
