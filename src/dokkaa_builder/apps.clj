(ns dokkaa-builder.apps
  (:require [k2nr.docker.core :as docker]
            [k2nr.docker.container :as container]))

(def apps (atom {}))

(defn choose-client
  "Randomly choose docker backend"
  []
  (docker/make-client "localhost:4243"))

(defn app->id [app-name user]
  (get-in @apps [(keyword app-name) :instances 0 :id]))

(defn create [app-name user image & {:keys [tag command port-bindings]}]
  (let [app-name (keyword app-name)
        cli (choose-client)]
    (if (nil? (app-name @apps))
      (let [id (docker/run cli image :tag tag
                                     :cmd command
                                     :port-bindings port-bindings)]
        (swap! apps assoc app-name {:image image
                                    :tag tag
                                    :user-id (:id user)
                                    :instances [{:host (.host cli)
                                                 :id id
                                                 :port-binding port-bindings}]})
        id)
      {:status 403
       :body (str "app " (name app-name) " already exists\n")})))

(defn update [req]
  )

(defn delete [app-name user]
  (let [instances (get-in @apps [(keyword app-name) :instances])]
    (doseq [i instances]
      (let [{host :host, id :id} i
            cli (docker/make-client host)]
        (container/stop cli id)
        (container/remove cli id)))))

(defn logs [app-name user]
  (let [cli (choose-client)
        resp (container/logs cli (app->id app-name user) :stdout true :stderr true)]
    (map #(str (name (:stream-type %))
               ": "
               (:body %)) resp)))
