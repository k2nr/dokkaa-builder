(ns dokkaa-builder.api
  (:require [cljs-http.client :as http]))

(defn apps [& {:keys [host token]}]
  (http/get (str "http://" host "/apps/") {:query-params {:token token}}))
