(ns dokkaa-builder.pages
  (:require [cemerick.austin.repls :refer (browser-connected-repl-js)]
            [hiccup.core :refer :all]
            [hiccup.page :as page]
            [hiccup.util :as util]))

(defn layout [& body]
  (page/html5
   [:head]
   [:body body]
   (page/include-js "/js/app.js")
   (page/include-css "/css/style.css")))

(defn index []
  (layout
   [:div {:id "my-app"}]))

(index)
