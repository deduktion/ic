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
  (> (/ (- (msec) (:last entry)) 1000.) interval))

(defn rescan
  "rescan a collection"
  [name]
  (info "rescan store: " name)
  (let [path (get (load-stores) name)
        interval (get (load-config) "interval")]
    (doseq [f (files path)]
      (if (.isFile f)
        (let [existing-entry (select-entry (str f))]
          (if existing-entry
            (if (check-needed? existing-entry interval)
              (update-entry existing-entry)
              (info "up-to-date: " (str f)))
            (insert-entry f)))))))
