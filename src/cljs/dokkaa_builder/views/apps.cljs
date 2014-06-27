(ns dokkaa-builder.views.apps
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [dokkaa-builder.api :as api]
            [cljs-http.util :refer [json-decode]]
            [cljs.core.async :refer [<! chan put!]]
            [om.core :as om :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            ))

(defn- refresh-apps! [app owner]
  (go
    (let [response (<! (api/get-apps "dokkaa.io:8080"))]
      (om/transact! app :apps (fn [_] (json-decode (:body response)))))))

(defn- delete-app! [app-name refresh]
  (go
    (let [resp (<! (api/delete-app "dokkaa.io:8080" app-name))]
      (put! refresh :apps))))

(defn app-view [app owner]
  (reify
    om/IRenderState
    (render-state [this {refresh :refresh}]
      (html
       [:tr
        [:td (:name app)]
        [:td (str (:image app) ":" (or (:tag app) "latest"))]
        [:td (:ps app)]
        [:td (:status app)]
        [:td
         [:button {:class (if (= (:status app) :creating)
                            "pure-button pure-button-disabled"
                            "pure-button")
                   :on-click (fn [e] (delete-app! (:name @app) refresh))}
          "Delete"]]]))))

(defn apps-view [app owner]
  (reify
    om/IInitState
    (init-state [_]
      {:refresh (chan)})

    om/IWillMount
    (will-mount [_]
      (refresh-apps! app owner)
      (go (let [c (om/get-state owner :refresh)]
            (loop []
              (let [refresh (<! c)]
                (condp = refresh
                  :apps (refresh-apps! app owner)))
              (recur)))))

    om/IRenderState
    (render-state [this {refresh :refresh}]
      (html [:div {:id "apps-view"}
             [:table {:class "pure-table"}
              [:thead
               [:tr
                [:td "Name"]
                [:td "Image"]
                [:td "Count"]
                [:td "Status"]
                [:td "Delete"]]]
              [:tbody
               (om/build-all app-view (:apps app)
                             {:init-state {:refresh refresh}})]]]))))
