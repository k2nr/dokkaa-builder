(ns dokkaa-builder.pages
  (:require [cemerick.austin.repls :refer [browser-connected-repl-js]]
            [hiccup.core :refer :all]
            [hiccup.page :as page]
            [hiccup.util :as util]))

(defn layout [& body]
  (page/html5
   [:head
    [:meta {:charset "utf-8"}]
    [:meta {:name "viewport"
            :content "width=device-width, initial-scale=1.0"}]
    [:meta {:name "description"
            :content "Dokkaa"}]
    [:title "Dokkaa. dokkaa docker"]
    (page/include-css "http://yui.yahooapis.com/pure/0.5.0/pure-min.css"
                      "/style.css")]
   [:body body]
   (page/include-js "/app.js")
   (when-let [repl (browser-connected-repl-js)]
     [:script repl])))

(defn index []
  (layout
   [:div {:id "layout"}
    [:div {:id "app"}]]))
