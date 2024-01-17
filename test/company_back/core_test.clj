(ns company-back.core-test
  (:require
   [clojure.data.json :as json]
   [clojure.test :refer [deftest is testing use-fixtures]]
   [company-back.core :as core]
   [company-back.http :as http]))

(def conn-spec
  {:host "localhost"
   :port 5432
   :username "postgres"
   :user "postgres"
   :password "postgres"
   :dbtype "postgres"
   :dbname "postgres"})

(defn setup-system [f]
  (core/stop-server)
  (core/start-system conn-spec)
  (f)
  (core/stop-server))

(use-fixtures :each setup-system)

(def ^:private product-endpoint
  "http://localhost:3000/product")

(defn- body [res]
  (some-> res
          :body
          (json/read-str :key-fn keyword)))

(deftest create
  (let [product {:name "triangle"
                 :quantity 42
                 :price 24}
        create! #(http/http-post product-endpoint {:body (json/write-str %) :headers {"Content-type" "application/json"}})
        update! #(http/http-put  product-endpoint {:body (json/write-str %) :headers {"Content-type" "application/json"}})
        delete! #(http/http-delete (str product-endpoint "/" %))
        create-response (create! product)]
    (testing "create product works"
      (is (= 200 (:status create-response))))
    (let [updated-response (update! (-> create-response body (assoc :quantity 41)))]
      (testing "update works"
        (let []
          (is (= 200 (:status updated-response)))
          (is (:_id (body updated-response)))))
      (testing "delete works"
        (let [delete-response (delete! (:_id (body updated-response)))]
          (is (= 200 (:status delete-response))))))))
