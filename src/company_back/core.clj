(ns company-back.core
  (:require
   [clojure.data.json :as json]
   [clojure.pprint :refer [pprint]]
   [clojure.spec.alpha :as spec]
   [company-back.db :as db]
   [company-back.view :as view]
   [compojure.core :refer [defroutes GET POST]]
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

(defn- search* []
  (let [db-products (db/fetch-all! @ds :product)
        amount (min 7 (+ 5 (rand-int (count db-products))))
        data (take amount (shuffle db-products))
        state (reduce (fn [acc p]
                        (-> acc
                            (update :price + (:price p))
                            (update :products conj p)))
                      {:name "total" :price 0 :products []}
                      data)
        response (:products (update state :products conj (select-keys state [:price :name])))]
    response))

(spec/def ::shape #{"triangle" "circle" " square "})
(defn- search
  [request]
  (let [shape (get (:query-params request) "shape")
        response
        (if-not (spec/valid? ::shape shape)
          (r/bad-request (json/write-str (spec/explain-data ::shape shape)))
          (r/response (json/write-str (search*))))]
    (r/header response "Content-Type" "application/pdf")))

(defn parse-payload
  [request]
  (-> request
      :body
      slurp
      (json/read-str :key-fn keyword)))

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
  (GET "/search/" request [request] (search request))
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
  (when @server ;; FIXME close connection
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
