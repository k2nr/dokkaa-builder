(ns dokkaa-builder.views.apps
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [dokkaa-builder.api :as api]
            [cljs-http.util :refer [json-decode]]
            [cljs.core.async :refer [<!]]
            [om.core :as om :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            ))

(defn app-view [app owner]
  (reify
    om/IRenderState
    (render-state [this state]
      (html
       [:li
        [:div
         [:span "Name: "]
         [:span (:name app)]]
        [:div
         [:span "Status: "]
         [:span (:status app)]]
        [:div
         [:span "Image: "]
         [:span (str (:image app) ":" (or (:tag app) "latest"))]]
        [:div
         [:span "container count: "]
         [:span (:ps app)]]
        [:button {:on-click (fn [e]
                              (go (let [resp (<! (api/delete-app
                                                  "dokkaa.io:8080"
                                                  (:name @app)))])))}
         "Delete"]]))))

(defn apps-view [app owner]
  (reify
    om/IWillMount
    (will-mount [_]
      (go
        (let [response (<! (api/apps "dokkaa.io:8080"))]
          (om/transact! app :apps (fn [_] (json-decode (:body response)))))))

    om/IRenderState
    (render-state [this state]
      (html [:div {:id "apps-view"
                   :class "pure-menu pure-menu-open"}
             [:ul (om/build-all app-view (:apps app))]]))))
