(ns company-back.http
  (:require
   [clj-http.client :as client]))

(defn http
  ([method url opts]
   (try (method url (merge {:async? false} opts))
        (catch Exception e
          (Throwable->map e)))))

(defn http-get
  ([url opts]
   (http client/get url opts)))

(defn http-post
  ([url opts]
   (http client/post url opts)))

(defn http-put
  ([url opts]
   (http client/put url opts)))

(defn http-delete
  ([url]
   (http-delete url {}))
  ([url opts]
   (http client/delete url opts)))

