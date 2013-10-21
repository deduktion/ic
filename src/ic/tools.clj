(ns ic.tools
  (:use [digest] :reload-all
        [clj-time.coerce :as tc]))

(def log-pattern "%d:%p:%c:: %m%n")
(def kb 1024)
(def human-readable-time-short "%02dm %02ds %03dms")
(def human-readable-time-long "%dh %02dmin %02ds %03dms")
(def units ["B " "kB" "MB" "GB" "TB" "PB" "EB" "ZB" "YB"])
(def units-sizes (map #(vector %1 %2) units (iterate #(* %1 kb) 1)))

(defn str>int [s] (Integer. (re-find #"[0-9]*" s)))
(defn exit [] (System/exit 0))
(defn blank [s] (clojure.string/blank? s))
(defn files [p] (file-seq (clojure.java.io/file p)))

(defn msec
  "current milliseconds"
  [] (System/currentTimeMillis))

(defn hdate
  "human readable date from"
  [timestamp]
  (tc/from-long timestamp))

(defn ftime
  "format ms in seconds with .3 millis"
  [ms]
  (format "%6.3fs" (/ (double ms) 1000.)))

(defn htime
  "human-readable-time"
  [ms]
  (let [sec (quot ms 1000)
        ms-offset (mod ms 1000)
        m (quot sec 60)
        s (mod sec 60)]
    (if (< m 60)
      (format human-readable-time-short m s ms-offset)
      (let [h (quot m 60)
            m (mod m 60)]
        (format human-readable-time-long h m s ms-offset)))))

(defn grab-unit
  "grab human readable size for"
  [bytes]
  (let [match (some #(if (< (/ bytes (second %)) kb)% false) units-sizes)]
    (format "%8.3f %s" (/ (double bytes) (second match)) (first match))))

(defn checksum
  "checksum of a file with algorithm"
  [file algorithm]
  (case algorithm "md5" (digest/md5 file)
                  "sha-1" (digest/sha-1 file)
                  "sha-256" (digest/sha-256 file)
                  "sha-512" (digest/sha-512 file)))
