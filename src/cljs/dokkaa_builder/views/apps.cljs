(ns dokkaa-builder.views.apps
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [dokkaa-builder.api :as api]
            [cljs-http.util :refer [json-decode]]
            [cljs.core.async :refer [<!]]
            [om.core :as om :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            ))

(defn apps-view [app owner]
  (reify
    om/IWillMount
    (will-mount [_]
      (go
        (let [response (<! (api/apps :host "dokkaa.io:8080" :token "dummy"))]
          (om/transact! app :apps (fn [_] (json-decode (:body response)))))))

    om/IRenderState
    (render-state [this state]
      (html [:div {:id "apps-view"}
             [:ul (map (fn [[k v]] [:li (name k)])
                       (:apps app))]]))))
