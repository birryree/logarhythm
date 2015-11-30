(ns logarhythm.stream-handler
  (:gen-class
   :implements [com.amazonaws.services.lambda.runtime.RequestStreamHandler])
  (:require [cheshire.core :refer :all] 
            [clojure.string :as string]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [amazonica.aws.s3 :as s3]
            [logarhythm.elb :as elb]))

(defn print-event [event]
  (log/debug (generate-string event {:pretty true}))
  event)

(defn handle-event [event]
  (let [bucket (get-in event [:records 0 :s3 :bucket :name])
        log-file (get-in event [:records 0 :s3 :object :key])]
    (log/info (format "ELB log file located in S3 bucket %s with path %s" bucket log-file))
    (cond
      (.endsWith log-file ".log") (elb/parse-and-send (get (s3/get-object bucket log-file) :input-stream))
      :else (log/info "Received an event that was not a .log file. Doing nothing..."))
    "Processing finished"))

(defn keyfn [key-string]
  (-> key-string
      (string/replace #"([a-z])([A-Z])" "$1-$2")
      (string/replace #"([A-Z+])([A-Z])" "$1-$2")
      (string/lower-case)
      (keyword)))

(defn -handleRequest [this is os context]
  (let [w (io/writer os)]
    (-> (parse-stream (io/reader is) keyfn)
        (handle-event)
        (generate-stream w))
    (.flush w)))
