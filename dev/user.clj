(ns dev.user
  (:require
   [clojure.tools.nrepl.server :as nrepl]
   [company-back.core :as company-back]))

(defonce nrepl-server (nrepl/start-server))
(spit "./.nrepl-port" (:port nrepl-server))

(defn start
  []
  (company-back/start-server))

(defn stop
  []
  (company-back/stop-server))

(defn- restart
  []
  (stop)
  (start))

(comment 

  (stop)
  (restart)

)

