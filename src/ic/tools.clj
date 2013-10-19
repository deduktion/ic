(ns ic.tools
  (:use [digest] :reload-all
        [clojure.tools.logging :only (info error)]
        [clj-logging-config.log4j]))
(def log-pattern "%d:%p:%c:: %m%n")
(set-logger! :pattern log-pattern)

(def kb 1024)
(defn msec [] (System/currentTimeMillis))
(def units ["B " "kB" "MB" "GB" "TB" "PB" "EB" "ZB" "YB"])
(def units-size)
(defn ftime [ms] (format "%6.3fs" (/ (double ms) 1000)))
(defn exit [] (System/exit 0))
(defn blank [s] (clojure.string/blank? s))
(defn files [p] (file-seq (clojure.java.io/file p)))
(defn checksum [file]
  "sha-256, more later"
  (digest/sha-256 file))

(defn grab-unit
 "comment"
  [bytes]
  (let [sizes (iterate #(* %1 kb) 1)
        units-sizes (map #(vector %1 %2) units sizes)
        unit-size   (some #(if (< (/ bytes (second %)) kb) % false) units-sizes)]
    (format "%8.3f %s" (/ (double bytes) (second unit-size)) (first unit-size))))
