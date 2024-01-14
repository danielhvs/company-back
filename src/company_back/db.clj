(ns company-back.db
  (:require
   [clojure.java.io :as io]
   [next.jdbc.result-set :as jdbc.rs]
   [honey.sql :as honey]
   [honey.sql.helpers :as hh]
   [next.jdbc :as jdbc]))

(defn format-sql [honey]
  (honey/format honey {:inline false}))

(defn fetch-all! [ds table]
  (let [query (-> (hh/select :name :quantity :price)
                  (hh/from (keyword table)))]
    (jdbc/execute! ds (format-sql query)
                   {:builder-fn jdbc.rs/as-unqualified-maps})))

(defn connect!
  [db-config]
  (prn "Connecting to Postgres: " (select-keys db-config [:user :db :port :host]))
  (jdbc/get-datasource db-config))

(def ^:private migrations
  ["DROP TABLE IF EXISTS product"
   (slurp (io/resource "schema.sql"))])

(def seed
  [{:name "Triangle", :quantity 36, :price 72}
   {:name "Circle", :quantity 46, :price 228}
   {:name "Square", :quantity 25, :price 200}
   {:name "Losangle", :quantity 24, :price 168}
   {:name "Triangle", :quantity 36, :price 72}
   {:name "Circle", :quantity 36, :price 203}
   {:name "Square", :quantity 13, :price 200}
   {:name "Losangle", :quantity 24, :price 168}
   {:name "Triangle", :quantity 36, :price 72}
   {:name "Circle", :quantity 47, :price 216}
   {:name "Square", :quantity 25, :price 188}
   {:name "Losangle", :quantity 24, :price 168}
   {:name "Triangle", :quantity 46, :price 72}
   {:name "Circle", :quantity 36, :price 206}
   {:name "Square", :quantity 25, :price 211}
   {:name "Losangle", :quantity 24, :price 168}])

(defn setup-initial-data!
  [ds]
  (println "setup-initial-data! " seed)
  (jdbc/execute! ds
                 (format-sql (-> (hh/insert-into :product)
                                 (hh/values seed)))))

(defn migrate! [ds]
  (when ds
    (doseq [migration migrations]
      (println migration)
      (jdbc/execute! ds [migration]))
    (setup-initial-data! ds)))
