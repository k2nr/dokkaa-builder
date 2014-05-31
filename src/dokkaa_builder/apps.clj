(ns dokkaa-builder.apps
  (:require [k2nr.docker.core :as docker]
            [k2nr.docker.container :as container]
            [dokkaa-builder.apps-router :as router]
            [dokkaa-builder.config :as config]
            [clojurewerkz.urly.core :as urly]
            [dokkaa-builder.redis :refer [wcar*]]
            [taoensso.carmine :as redis]))

(declare delete-instances)

(defn app [user app-name]
  (wcar* (redis/get (str "apps:" app-name))))

(defn set-app [app-name app]
  (wcar* (redis/set (str "apps:" app-name) app)))

(defn delete-app [app-name]
  (wcar* (redis/del (str "apps:" app-name))))

(defn- pick-backends
  "Randomly choose docker backend"
  []
  (let [cli (docker/make-client "127.0.0.1:4243")]
    [{:client cli
      :port (router/generate-host-port (.host cli))}]))

(defn- default-frontend-url [app-name]
  (str app-name "." (config/domain-name)))

(defn- upstream-url [cli port]
  (let [host (urly/url-like (str "http://" (.host cli)))
        url (str "http://" (urly/host-of host) ":" port)]
    url))

(defn instances [app-name]
  (let [app (wcar* (redis/get (str "apps:" app-name)))]
    (:instances app)))

(defn create [app-name user image & {:keys [tag command port]}]
  (let [backends (pick-backends)
        front-url (default-frontend-url app-name)
        old-isntances (instances app-name)
        new-instances (vec (for [{cli :client host-port :port} backends]
                             (let [id (docker/run cli image
                                        :tag tag
                                        :cmd command
                                        :port-bindings {host-port port})
                                   upstream (upstream-url cli host-port)]
                               {:id id, :host upstream})))]
    (when old-isntances (router/delete-domain front-url))
    (apply router/add-domain front-url (map :host new-instances))
    (set-app app-name {:image      image
                       :tag        tag
                       :user-id    (:id user)
                       :front-urls [front-url]
                       :instances  new-instances})
    (when old-isntances (delete-instances old-isntances))
    nil))

(defn update [req]
  )

(defn delete [app-name user]
  (let [is (instances app-name)]
    (delete-instances is)
    (delete-app app-name)
    (router/delete-domain (default-frontend-url app-name))
    nil))

(defn delete-instances [instances]
  (doseq [i instances]
    (let [{host :host, id :id} i
          cli (docker/make-client (str (urly/host-of (urly/url-like host)) ":4243"))]
      (container/stop cli id)
      (container/remove cli id))))

(defn logs [app-name user]
  (let [{cli :client} (first (pick-backends))
        resp (container/logs cli (-> app-name instances first :id) :stdout true :stderr true)]
    (map #(str (name (:stream-type %))
               ": "
               (:body %)) resp)))
