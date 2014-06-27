(ns dokkaa-builder.views.create-app
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [dokkaa-builder.api :as api]
            [cljs.core.async :refer [<!]]
            [om.core :as om :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            ))

(defn- node-value [owner ref]
  (-> (om/get-node owner ref)
      .-value))

(defn- app-properties [owner]
  {:app-name (node-value owner "app-name")
   :image (node-value owner "image-name")
   :tag (node-value owner "tag")
   :command (node-value owner "command")
   :port (node-value owner "port")
   :ps   (node-value owner "ps")})

(defn- descripted-input-block [desc opts]
  [:tr
   [:td desc]
   [:td [:input (merge {:type "text"} opts)]]])

(defn- create-app! [app owner]
  (go (let [properties (app-properties owner)
            resp (<! (api/create-app "dokkaa.io:8080"
                                     (:app-name properties)
                                     (dissoc properties :app-name)))])))

(defn create-app-view [app owner]
  (reify
    om/IRenderState
    (render-state [this state]
      (html [:div
             [:table
              (descripted-input-block "App Name" {:ref "app-name"})
              (descripted-input-block "Image" {:ref "image-name"})
              (descripted-input-block "Tag" {:ref "tag"})
              (descripted-input-block "Command" {:ref "command"})
              (descripted-input-block "Port" {:ref "port"})
              (descripted-input-block "PS" {:ref "ps"})
              [:tr
               [:td
                [:div
                 [:button {:class "pure-button pure-button-primary"
                           :on-click (fn [e] (create-app! app owner))}
                  "Create"]]]]]]))))
