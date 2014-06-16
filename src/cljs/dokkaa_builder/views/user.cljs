(ns dokkaa-builder.views.user
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [dokkaa-builder.api :as api]
            [cljs-http.util :refer [json-decode]]
            [cljs.core.async :refer [<! chan put! timeout]]
            [om.core :as om :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            ))

(defn- refresh-token-list! [app owner]
  (go
    (let [tokens (-> (<! (api/get-access-tokens "dokkaa.io:8080"))
                     :body
                     json-decode)]
      (om/transact! app :api-tokens (fn [_] tokens)))))

(defn- delete-token! [owner]
  (go
    (let [token (-> (om/get-node owner "token") .-text)
          {status :status} (<! (api/delete-access-token "dokkaa.io:8080" token))]
      (= status 200))))

(defn- add-access-token! [app]
  (go
    (let [{body :body} (<! (api/add-access-token "dokkaa.io:8080"))]
      (om/transact! app :api-token #(conj % body)))))

(defn- token-view [token owner]
  (reify
    om/IRenderState
    (render-state [this {:keys [refresh]}]
      (html [:li
             [:a {:ref "token"} token]
             [:button {:class "pure-button"
                       :on-click (fn [_]
                                   (delete-token! owner)
                                   (put! refresh :tokens))}
              "Delete"]]))))

(defn user-view [app owner]
  (reify
    om/IInitState
    (init-state [_]
      {:refresh (chan)})

    om/IWillMount
    (will-mount [_]
      (refresh-token-list! app owner)
      (let [refresh-chan (om/get-state owner :refresh)]
        (go (loop []
              (let [refresh (<! refresh-chan)]
                (<! (timeout 100))
                (condp = refresh
                  :tokens (refresh-token-list! app owner)))
              (recur)))))

    om/IRenderState
    (render-state [this {:keys [refresh] :as state}]
      (html [:div
             [:div {:id "app-view"
                    :class "pure-menu pure-menu-open"}
              [:ul
               (om/build-all token-view (:api-tokens app)
                             {:init-state {:refresh refresh}})]]
             [:div
              [:button {:class "pure-button pure-button-primary"
                        :on-click (fn [_]
                                    (add-access-token! app)
                                    (put! refresh :tokens))}
               "New Token"]]]))))
