(ns clj-docker.core
  (:require [clj-docker.client :as client]))

(defn version [cli]
  (client/get cli "/version" {}))
