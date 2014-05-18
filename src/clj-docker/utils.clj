(ns clj-docker.utils)

(defn map-keys [f m]
  (into {} (map (fn [[a b]] [(f a) b]) m)))

(defn ^bytes read-bytes [stream n]
  (let [buf (byte-array n)]
    (.read stream buf 0 n)
    buf))

(defn ^BigInteger bytes->int
  [^bytes bs & {:keys [little-endian]
                :or {little-endian false}}]
  (let [bs (if little-endian (reverse bs) bs)]
    (biginteger bs)))

(defn bytes->string [bs]
  (apply str (map char bs)))

(defn raw-stream->map [stream]
  (let [[stream-type _ _ _] (vec (read-bytes stream 4))
        size (bytes->int (read-bytes stream 4))
        body (bytes->string (read-bytes stream size))]
    {:stream-type stream-type
     :body body}))
