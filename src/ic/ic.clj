(ns ic.ic
  (:use [ic.tools]
        [ic.config]
        [clojure.java.jdbc :exclude (resultset-seq)]
        [clojure.tools.logging :only (info error)]
        [clj-logging-config.log4j]))
(set-logger! :pattern log-pattern)

(defn rescan-path [path]
  "rescan a collection"
  (info "rescan-path: " path)
  (doseq [f (files path)]
    (if (.isFile f)
      (let [now (msec)
            size (.length f)
            chksum (checksum f)
            took (- (msec) now)]
          (info chksum " " (grab-unit size) " " (ftime took) " " (str f))
          (try (insert-records
              :intchk
              {:path (str f) :chksum chksum :size size
               :took took    :first now     :last now})
          (catch Exception e (error e)))))))

(defn rescan [label path]
  "rescan via name, load path if exists else add"
  (info "rescan: " label " " path)
  (rescan-path path)
  (exit))

(defn add-store [label path]
  "add a new store"
  (info "add a new store " label " " path))

(defn list-stores []
  "list all stores or just with given name"
  (println "show all stores...")
  (exit))

(defn show-banner [banner]
  "show banner and exit"
  (println banner)
  (exit))
