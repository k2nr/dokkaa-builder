(ns k2nr-utils.file
  (:require [clojure.java.io :refer [file]])
  (:import (org.apache.commons.io FilenameUtils)))

(defn basename [path]
  (FilenameUtils/getBaseName path))

(defn path-exists? [p]
  (.exists (file p)))
