(ns company-back.db
  (:require
   [clojure.java.io :as io]
   [clojure.string :as str]
   [honey.sql :as honey]
   [honey.sql.helpers :as hh]
   [next.jdbc :as jdbc]
   [next.jdbc.result-set :as jdbc.rs]))

(defn format-sql [honey]
  (honey/format honey {:inline false}))

(def ^:private jdbc-opts
  {:builder-fn jdbc.rs/as-unqualified-maps})

(defn fetch-all!
  [ds table where]
  (let [query (-> (hh/select :_id :name :quantity :price)
                  (hh/from (keyword table))
                  (hh/where where))]
    (jdbc/execute! ds (format-sql query) jdbc-opts)))

(defn delete!
  [ds table where]
  (let [query (-> (hh/delete-from (keyword table))
                  (hh/where where))]
    (jdbc/execute! ds (format-sql query) jdbc-opts)))

(defn connect!
  [db-config]
  (prn "Connecting to Postgres: " (select-keys db-config [:user :db :port :host]))
  (jdbc/get-datasource db-config))

(def ^:private migrations
  ["DROP TABLE IF EXISTS product"
   (slurp (io/resource "schema.sql"))])

(def seed
  [{:name "triangle", :quantity 36, :price 72}
   {:name "triangle", :quantity 57, :price 84}
   {:name "triangle", :quantity 24, :price 59}
   {:name "circle", :quantity 37, :price 228}
   {:name "triangle", :quantity 18, :price 72}
   {:name "circle", :quantity 36, :price 203}
   {:name "circle", :quantity 40, :price 216}
   {:name "circle", :quantity 44, :price 195}
   {:name "square", :quantity 25, :price 210}
   {:name "square", :quantity 13, :price 200}
   {:name "square", :quantity 17, :price 188}
   {:name "square", :quantity 38, :price 201}])

(defn setup-initial-data!
  [ds]
  (println "setup-initial-data! " seed)
  (let [assoc-uid (fn [p] (assoc p :_id (->> (str/replace (str (random-uuid)) #"-" "")
                                             (take 24)
                                             (apply str))))]
    (jdbc/execute! ds
                   (format-sql (-> (hh/insert-into :product)
                                   (hh/values (map assoc-uid seed)))))))

(defn migrate! [ds]
  (when ds
    (doseq [migration migrations]
      (println migration)
      (jdbc/execute! ds [migration]))
    (setup-initial-data! ds)))


