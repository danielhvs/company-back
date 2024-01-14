(ns dev.user
  (:require
   [clojure.tools.nrepl.server :as nrepl]
   [company-back.core :as company-back]))

(defonce nrepl-server (nrepl/start-server))
(spit "./.nrepl-port" (:port nrepl-server))

(def conn-spec
  {:host "localhost"
   :port 5432
   :username "postgres"
   :user "postgres"
   :password "postgres"
   :dbtype "postgres"
   :dbname "postgres"})

(defn start
  []
  (company-back/start-system conn-spec))

(defn stop
  []
  (company-back/stop-server))

(defn- restart
  []
  (stop)
  (start))

(comment

  (stop)
  (restart))

