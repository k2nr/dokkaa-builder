(ns dokkaa-builder.apps-router)

(defn add-upstream [domain upstream]
  )

(defn delete-upstream [domain upstream]
  )

(defn add-domain [domain & upstreams]
  (doseq [u upstreams]
    (add-upstream domain u)))

(defn delete-domain [domain]
  )
