(ns ic.ic
  (:use [ic.tools]
        [ic.config]
        [ic.db]
        [ic.stores]
        [clojure.java.jdbc :exclude (resultset-seq)]
        [clojure.tools.logging :only (info error)]
        [clj-logging-config.log4j]))
(set-logger! :pattern log-pattern)

(defn check-needed?
  "check if the last rescan > interval"
  [entry interval]
  (> (/ (- (msec) (:lastscan entry)) 1000.) interval))

(defn rescan-file
  "rescan a single file"
  [path file interval algorithm]
  (let [existing-entry (select-entry (str file) algorithm)]
    (if existing-entry
      (if (check-needed? existing-entry interval)
        (update-entry path file algorithm))
      (insert-entry path file algorithm))))

(defn rescan
  "rescan a collection"
  [name]
  (info "rescan store: " name)
  (let [path (get (load-stores) name)
        config (load-config)
        interval (get config "interval")
        algorithm (get config "algorithm")]
    (doseq [file (files path)]
      (if (.isFile file) (rescan-file path file interval algorithm)))))

(defn rescan-all
  "rescan all stores"
  []
  (info "rescan-all:  "  (load-stores))
  (doseq [store (keys (load-stores))] (rescan store)))
