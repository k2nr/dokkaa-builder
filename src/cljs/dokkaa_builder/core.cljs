(ns dokkaa-builder.core
  (:require [clojure.browser.repl]
            [om.core :as om :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [dokkaa-builder.views.apps :refer [apps-view]]
            [dokkaa-builder.views.create-app :refer [create-app-view]]
            ))

(enable-console-print!)

(def app-state
  (atom {:current-view :apps}))

(def views {:apps apps-view
            :create-app create-app-view})

(defn change-view [app view-name]
  (om/transact! app :current-view (fn [_] view-name)))

(defn side-menu-view [app owner]
  (reify
    om/IRender
    (render [_]
      (html [:div {:id "menu"
                   :class "pure-menu pure-menu-open"}
             [:a {:class "pure-menu-heading"} "Dokkaa"]
             [:ul (map (fn [[k v]]
                         [:li
                          [:a {:on-click (fn [_] (change-view app k))
                               :class (when (= k (:current-view app))
                                        "pure-menu-selected")}
                           (name k)]])
                       views)]]))))

(defn root-view [app owner]
  (reify
    om/IRender
    (render [_]
      (html [:div {:id "main"}
             [:div {:class "header"}
              [:h2 "Subtitle"]]
             [:div {:class "content"}
              (om/build side-menu-view app)
              (om/build (views (:current-view app)) app)]]))))

(om/root root-view app-state
  {:target (. js/document (getElementById "app"))})
