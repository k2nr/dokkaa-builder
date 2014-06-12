(ns dokkaa-builder.etcd
  (:require [clj-http.client :as http]
            [cheshire.core :as json])
  (:refer-clojure :exclude [set get]))

(defn remove-trailing-slash [url]
  (if (and url (.endsWith url "/"))
    (recur (.substring url 0 (dec (count url))))
    url))

(defn key->url [base-url k]
    (if (and k (.startsWith k "/"))
      (recur base-url (clojure.string/replace-first k #"/" ""))
      (str base-url "/v2/keys/" k)))

(defn- query-params [url {:keys [prev-val prev-index prev-exist wait recursive]}]
  (let [params (cond-> []
                       prev-val (conj (str "prevValue=" prev-val))
                       prev-index (conj (str "prevIndex=" prev-index))
                       prev-exist (conj (str "prevExist=" prev-exist))
                       wait (conj (str "wait=" wait))
                       recursive (conj (str "recursive=" recursive)))]
    (if (empty? params)
      url
      (str url "?" (clojure.string/join "&" params)))))

(defn- param-map [{:keys [value ttl]}]
  (cond-> {}
          value (merge {:value value})
          ttl (merge {:ttl ttl})))

(defn- parse-response [resp]
  (json/parse-string (:body resp) true))

(defn -invoke
  ([base-url f k]
     (-invoke base-url f k {}))
  ([base-url f k {:keys [] :as keys}]
     (let [url (-> base-url
                   (remove-trailing-slash)
                   (key->url k)
                   (query-params (select-keys keys [:prev-val
                                                    :prev-index
                                                    :prev-exist
                                                    :wait
                                                    :recursive])))]
       (try
         (-> url
             (f {:form-params (param-map (select-keys keys [:value :ttl]))})
             (parse-response))
         (catch Exception e
           nil)))))

(defn set [base-url k v & {:keys [] :as keys}]
  "Will set the value of a key, the following options are
   allowed: :ttl, :prev-val :prev-index and :prev-exist."
  (-invoke base-url
           http/put
           k
           (merge {:value v} keys)))

(defn get [base-url k]
  "Perform a listing of the given key, returning a map of its
   details, or nil if the key does not exist."
  (-invoke base-url http/get k))

(defn exists? [base-url k]
  "Returns true if the key exists."
  (not (nil? (get base-url k))))

(defn dir? [base-url k]
  "Returns true if the key exists and is a directory."
  (true? (:dir (get base-url k))))

(defn delete! [base-url k]
  "Performs a delete of the specified key."
  (-invoke base-url http/delete k))

(defn wait [base-url k & {:keys [] :as keys}]
  "Takes an instance and returns a future, blocking until a
   change is made on the requested key. Valid options are
   :recursive true."
  (future (-invoke base-url
                   http/get
                   k
                   (merge {:wait true} keys))))

(defn machines [admin-url]
  (parse-response (http/get (-> admin-url
                                remove-trailing-slash
                                (str "/v2/admin/machines")))))
