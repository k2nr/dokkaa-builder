(ns dokkaa-builder.views.create-app
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [dokkaa-builder.api :as api]
            [cljs.core.async :refer [<!]]
            [om.core :as om :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            ))

(defn node-value [owner ref]
  (-> (om/get-node owner ref)
      .-value))

(defn app-properties [owner]
  {:app-name (node-value owner "app-name")
   :image-name (node-value owner "image-name")
   :tag (node-value owner "tag")
   :command (node-value owner "command")
   :port (js/parseInt (node-value owner "port"))})

(defn descripted-input-block [desc opts]
  [:div
   [:span desc]
   [:input (merge {:type "text"} opts)]])

(defn create-app-view [app owner]
  (reify
    om/IRenderState
    (render-state [this state]
      (html [:div
             (descripted-input-block "App Name" {:ref "app-name"})
             (descripted-input-block "Image" {:ref "image-name"})
             (descripted-input-block "Tag" {:ref "tag"})
             (descripted-input-block "Command" {:ref "command"})
             (descripted-input-block "Port" {:ref "port"})
             [:div
              [:button {:class "pure-button pure-button-primary"
                        :on-click (fn [e] (js/alert (str (app-properties owner))))} "Create"]]]))))
