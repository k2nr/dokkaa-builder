(ns dokkaa-builder.views.create-app
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [dokkaa-builder.api :as api]
            [cljs.core.async :refer [<!]]
            [om.core :as om :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            ))

(defn create-app-view [app owner]
  (reify
    om/IRenderState
    (render-state [this state]
      (html [:div
             [:span "Image name"]
             [:button {:on-click (fn [e] (js/alert "hello"))} "Create"]]))))
