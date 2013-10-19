(ns ic.ic
  (:use [ic.tools]
        [ic.config]
        [clojure.java.jdbc :exclude (resultset-seq)]
        [clojure.tools.logging :only (info error)]
        [clj-logging-config.log4j]))
(set-logger! :pattern log-pattern)

(defn rescan
  "rescan a collection"
  [name]
  (info "rescan store: " name)
  (let [path (get (load-stores) name)]
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
            (catch Exception e (error e))))))))
