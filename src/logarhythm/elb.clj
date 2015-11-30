(ns ^{:author "William Lee"
      :doc "Parser for ELB-formatted log data"}
  logarhythm.elb
  (:require [clojure-csv.core :as csv]
            [clojure.java.io :as io]
            [semantic-csv.core :refer :all]
            [clojure.string :as string]
            [amazonica.aws.sqs :as sqs]
            [clojure.tools.logging :as log]
            [cheshire.core :as cheshire]))

; The header for the ELB logs
(def log-header [:timestamp :elb :clientIp :backendIp :request_time
                 :backend_time :response_time :elb_status :server_status
                 :received :sent :request :user_agent :ssl_cipher :ssl_protocol])

; Just return the value we already had
(defn handle-cast-exception [colname value]
  value)

(def cast-map
  {
    :server_status #(Integer/parseInt %)
    :elb_status #(Integer/parseInt %)
    :backend_time #(Double/parseDouble %)
    :response_time #(Double/parseDouble %)
    :received #(Integer/parseInt %)
    :sent #(Integer/parseInt %)
    :request_time #(Double/parseDouble %)
  })

(defn parse-log [logfile]
  (with-open [in-file (io/reader logfile)]
    (->>
      (csv/parse-csv in-file :delimiter \space)
      (mappify {:header log-header})
      (cast-with cast-map {:exception-handler handle-cast-exception})
      doall)))

; process a log-entry map into a proper map and then returns the updated map
(defn process-log-entry [entry]
  (let [server (zipmap [:server_address :server_port] (string/split (get-in entry [:backendIp]) #":"))
        client (zipmap [:client_address :client_port] (string/split (get-in entry [:clientIp]) #":"))
        request (zipmap [:method :request_uri :http_version] (string/split (get-in entry [:request]) #"\s+"))
        mmap (merge entry server client request)]
        (dissoc mmap :request :backendIp :clientIp)))

(defn remap-request [msgs]
  ; TODO rewrite this - this is a pretty ugly sequence of function calls
  (map #(apply hash-map %) (map #(interleave [:id :messageBody] %) (map vector (map str (range 0 (count msgs))) msgs)))) 

(defn enqueue-message [queue msg]
  (sqs/send-message queue msg))

(defn enqueue-messages [queue msgs]
  (let [batch-message (vec (remap-request msgs))]
    (do 
      (sqs/send-message-batch :queueUrl queue :entries batch-message))))

(defn process-message [entry]
  (->>
    (process-log-entry entry)
    (cheshire/generate-string)))

(defn parse-and-send [queue logfile]
  (let [log-entries (parse-log logfile)]
    (do
      (log/info (format "Parsing and sending %d ELB log lines" (count log-entries)))
      (dorun (pmap (partial enqueue-messages queue) (partition-all 10 10 (map process-message log-entries))))
      (log/info "Finished processing"))))
