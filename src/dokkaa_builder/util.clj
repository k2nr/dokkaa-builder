(ns dokkaa-builder.util
  (:require [environ.core :refer [env]]))

(defn stage []
  (or (env :stage) "development"))

(defn in-dev? []
  (= (stage) "development"))
