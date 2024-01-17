(ns company-back.core
  (:require
   [clojure.data.json :as json]
   [clojure.pprint :refer [pprint]]
   [clojure.spec.alpha :as spec]
   [company-back.db :as db]
   [company-back.view :as view]
   [compojure.core :refer [defroutes DELETE GET POST PUT]]
   [compojure.handler :refer [site]]
   [compojure.route :as route]
   [environ.core :as env]
   [ring.adapter.jetty :as jetty]
   [ring.middleware.cors :refer [wrap-cors]]
   [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
   [ring.middleware.multipart-params :refer [wrap-multipart-params]]
   [ring.util.io :as ring]
   [ring.util.response :as r])
  (:gen-class))

(defonce server (atom nil))
(defonce ds (atom nil))

(defn parse-payload
  [request]
  (let [body (slurp (:body request))]
    (json/read-str body :key-fn keyword)))

(defn- delete
  [id]
  (db/delete! @ds :product [:= :_id id])
  (r/response (json/write-str {:_id id})))

(defn ->product [m]
  (select-keys m [:name :price :quantity :_id]))

(spec/def ::create-shape #{"triangle" "circle" "square"})
(defn- create-product
  [request]
  (let [{:keys [name _id] :as product} (-> request parse-payload ->product)]
    (if-not (spec/valid? ::create-shape name)
      (r/bad-request (json/write-str (spec/explain-data ::create-shape name)))
      (->> (assoc product :_id (db/new-id))
           (db/insert-one! @ds :product)
           json/write-str
           r/response))))

(defn- update-product
  [request]
  (let [{:keys [_id] :as product} (-> request parse-payload ->product)]
    (db/update-one! @ds :product
                    [:= :_id _id]
                    product)
    (r/response (json/write-str {:_id _id}))))

(defn- search!
  [shape]
  (db/fetch-all! @ds :product
                 (if (= shape "all")
                   [:= 1 1]
                   [:= :name shape])))

(spec/def ::shape-search #{"triangle" "circle" "square" "all"})
(defn- search
  [request]
  (let [shape (get (:query-params request) "shape")
        response
        (if-not (spec/valid? ::shape-search shape)
          (r/bad-request (json/write-str (spec/explain-data ::shape-search shape)))
          (r/response (json/write-str (search! shape))))]
    (r/header response "Content-Type" "application/pdf")))

(defn make-pdf*
  [data]
  (let [pdf-input-data (parse-payload data)]
    (ring/piped-input-stream
     (view/pdf pdf-input-data))))

(defn- make-pdf
  [request]
  (-> (r/response (make-pdf* request))
      (r/header "Access-Control-Allow-Origin" "*")
      (r/header "Content-Type" "application/pdf")
      (r/header "Content-disposition" "attachment; filename=\"yourfile.pdf\"")))

(defroutes app-routes
  (POST "/make-pdf" request [request] (make-pdf request))
  (PUT "/product" request [request] (update-product request))
  (POST "/product" request [request] (create-product request))
  (DELETE "/product/:id" [id] (delete id))
  (GET "/product" request [request] (search request))
  (route/not-found "Not Found"))

(defn wrap-debug
  [handler]
  (fn [request]
    (pprint "request ---------")
    (pprint request)
    (pprint "request ---------")
    (let [response (handler request)]
      (pprint "response ---------")
      (pprint response)
      (pprint "response ---------")
      response)))

(def app
  (-> app-routes
      (wrap-defaults (assoc-in site-defaults [:security :anti-forgery] false)) ;; FIXME security breach while downloading pdf
      (wrap-cors :access-control-allow-origin [#".*"]
                 :access-control-allow-methods [:get :put :post :delete])
      wrap-multipart-params
      wrap-debug))

(defn stop-server
  []
  (when @server
    (.stop @server)))

(defn start-system
  [conn-spec]
  (when-let [data-source (db/connect! conn-spec)]
    (let [port (Integer. (or (env/env :port) 3000))]
      (reset! ds data-source)
      (db/migrate! data-source)
      (reset! server (jetty/run-jetty (site #'app) {:port port :join? false})))))

(defn -main [& args]
  (start-system (first args)))
