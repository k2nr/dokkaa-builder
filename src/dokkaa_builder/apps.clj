(ns dokkaa-builder.apps
  (:require [k2nr.docker.core :as docker]))

(def apps (atom {}))

(defn choose-client
  "Randomly choose docker backend"
  []
  (docker/make-client "localhost:4243"))

(defn create [app-name user image & {:keys [tag command port-bindings]}]
  (let [app-name (keyword app-name)
        cli (choose-client)]
    (if (nil? (app-name @apps))
      (do
        (swap! apps assoc app-name {:image image
                                    :tag tag
                                    :user-id (:id user)
                                    :instances [{:host (.host cli)
                                                 :port-binding port-bindings}]})
        (docker/run cli image :tag tag
                              :cmd command
                              :port-bindings port-bindings))
      {:status 403
       :body (str "app " (name app-name) " already exists\n")})))

(defn update [req]
  )

(defn delete [req]
  )
