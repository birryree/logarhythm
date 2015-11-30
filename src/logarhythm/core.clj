(ns logarhythm.core
  (:gen-class
   :methods [^:static [handler [String] String]]))

(defn -handler [s]
  (str "hello " s "!"))
