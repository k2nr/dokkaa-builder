(ns dokkaa-builder.pages
  (:require [cemerick.austin.repls :refer [browser-connected-repl-js]]
            [hiccup.core :refer :all]
            [hiccup.page :as page]
            [hiccup.util :as util]))

(defn layout [& body]
  (page/html5
   [:head]
   [:body body]
   (page/include-js "/app.js")
   (page/include-css "/style.css")
   (when-let [repl (browser-connected-repl-js)]
     [:script repl])))

(defn index []
  (layout
   [:div {:id "my-app"}]))
