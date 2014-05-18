(ns clj-docker.image
  (:require [clj-docker.client :as client]
            [cheshire.core :as json]
            [k2nr-utils.tar  :refer [create-archive with-open-tar add-entry]]
            [clojure.java.io :refer [file input-stream delete-file]]))

(defn build-from-stream [cli stream & {:keys [name quiet no-cache]}]
  (client/post cli "/build"
               {:headers {"Content-Type" "application/tar"}
                :query-params {:t name
                               :q quiet
                               :nocache no-cache}
                :body stream
                :as :stream}))

(defn build-from-dir [cli path & opts]
  (let [tar (create-archive path)
        response (build-from-stream cli (input-stream tar) opts)]
    (delete-file tar)
    response))

(defn build-from-file [cli path & opts]
  (let [docker-file (file path)
        tar (with-open-tar
              "dockerfile.tar.gz"
              (fn [tar]
                (add-entry tar docker-file "Dockerfile")))
        response (build-from-stream cli (input-stream tar) opts)]
    (delete-file tar)
    response))

(defn create-image [cli name & {:keys [repo tag registry]}]
  (client/post cli "/images/create"
               {:query-params {:fromImage name
                               :repo      repo
                               :tag       tag
                               :registry  registry}
                :as :stream}))
