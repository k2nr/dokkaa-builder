(ns dokkaa-builder.apps
  (:require [k2nr.docker.core :as docker]
            [k2nr.docker.container :as container]
            [dokkaa-builder.apps-router :as router]
            [dokkaa-builder.config :as config]
            [clojurewerkz.urly.core :as urly]))

(def apps (atom {}))

(defn- generate-host-port [host]
  (+ 10000 (rand-int 1000)))

(defn- choose-backends
  "Randomly choose docker backend"
  []
  (let [cli (docker/make-client "127.0.0.1:4243")]
    [{:client cli
      :port (generate-host-port (.host cli))}]))

(defn- default-frontend-url [app-name]
  (str app-name "." (config/domain-name)))

(defn- upstream-url [cli port]
  (let [host (urly/url-like (str "http://" (.host cli)))
        url (str "http://" (urly/host-of host) ":" port)]
    url))

(defn app->id [app-name user]
  (get-in @apps [(keyword app-name) :instances 0 :id]))

(defn create [app-name user image & {:keys [tag command port]}]
  (let [backends (choose-backends)
        front-url (default-frontend-url app-name)
        old-isntances (get-in @apps [(keyword app-name) :instances])
        new-instances (doall (for [{cli :client host-port :port} backends]
                           (let [id (docker/run cli image
                                      :tag tag
                                      :cmd command
                                      :port-bindings {host-port port})
                                 upstream (upstream-url cli host-port)]
                             {:id id, :host upstream})))]
    (when old-isntances (router/delete-domain front-url))
    (apply router/add-domain front-url (map :host new-instances))
    (swap! apps assoc (keyword app-name) {:image      image
                                          :tag        tag
                                          :user-id    (:id user)
                                          :front-urls [front-url]
                                          :instances  new-instances})
    (when old-isntances (delete-instances old-isntances))
    nil))

(defn update [req]
  )

(defn delete [app-name user]
  (let [instances (get-in @apps [(keyword app-name) :instances])]
    (delete-instances instances)))

(defn delete-instances [instances]
  (doseq [i instances]
    (let [{host :host, id :id} i
          cli (docker/make-client (str (urly/host-of (urly/url-like host)) ":4243"))]
      (container/stop cli id)
      (container/remove cli id))))

(defn logs [app-name user]
  (let [{cli :client} (first (choose-backends))
        resp (container/logs cli (app->id app-name user) :stdout true :stderr true)]
    (map #(str (name (:stream-type %))
               ": "
               (:body %)) resp)))
