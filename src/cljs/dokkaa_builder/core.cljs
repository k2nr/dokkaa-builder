(ns dokkaa-builder.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [clojure.browser.repl]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :refer [put! chan <!]]
            [sablono.core :as html :refer-macros [html]]
            [clojure.data :as data]
            [clojure.string :as string]))

(enable-console-print!)

(def app-state
  (atom {}))

(defn side-menu-view [app owner]
  (reify
    om/IRender
    (render [_]
      (html [:div {:id "side-menu"}
             [:ul]]))))

(defn apps-view [app owner]
  (reify
    om/IRender
    (render [_]
      (html [:div {:id "main-view"}
             [:ul]]))))

(defn root-view [app-owner]
  (reify
    om/IRender
    (render [_]
      (html [:div
             (om/build side-menu-view nil)
             (om/build apps-view nil)]))))

(om/root root-view app-state
  {:target (. js/document (getElementById "app"))})
