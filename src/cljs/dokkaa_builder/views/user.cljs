(ns dokkaa-builder.views.user
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [dokkaa-builder.api :as api]
            [cljs-http.util :refer [json-decode]]
            [cljs.core.async :refer [<!]]
            [om.core :as om :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            ))


(defn user-view [app owner]
  (reify
    om/IWillMount
    (will-mount [_]
      (go
        (let [response (<! (api/get-access-tokens "dokkaa.io:8080"))]
          (om/transact! app :api-tokens (fn [_] (json-decode (:body response)))))))

    om/IRenderState
    (render-state [this state]
      (html [:div {:id "app-view"}
             [:ul (map (fn [t] [:li t])
                       (:api-tokens app))]
             [:div
              [:button {:class "pure-button pure-button-primary"
                        :on-click
                        (fn [e]
                          (go
                            (let [{body :body} (<! (api/add-access-token "dokkaa.io:8080"))]
                             (om/transact! app :api-tokens #(conj % body)))))}
               "New Token"]]]))))
