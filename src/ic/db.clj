(ns ic.db
  (:use [ic.tools]
        [ic.config]
        [clojure.java.jdbc :exclude (resultset-seq)]
        [clojure.tools.logging :only (info error)]
        [clj-logging-config.log4j]))
(set-logger! :pattern log-pattern)

(def db {
    :classname "org.sqlite.JDBC"
    :subprotocol "sqlite"
    :subname db-path})

(defn create-db
  "create database"
  []
  (try
    (with-connection db
      (create-table
        :ic [:prefix :text]
            [:path :text]
            [:chksum :text]
            [:size :integer]
            [:took :integer]
            [:firstscan :integer]
            [:lastscan :integer]
            [:lastmodified :integer]
            [:r :boolean]
            [:w :boolean]
            [:x :boolean]
            [:hidden :boolean]))
    (catch Exception e (error e))))

(try (create-db)
 (catch java.sql.BatchUpdateException e (error e))
 (catch Exception e (error e)))

(defn create-record
  "create a new record"
  [path file]
  (let [now (msec)
        size (.length file)
        chksum (checksum file)
        took (- (msec) now)
        filepath (str file)
        last-modified (.lastModified file)
        r (.canRead file)
        w (.canWrite file)
        x (.canExecute file)
        hidden (.isHidden file)]
    {:prefix path :path filepath :chksum chksum
     :size size :took took
     :firstscan now :lastscan now
     :lastmodified last-modified
     :r r :w w :x x :hidden hidden}))

(defn records-equal?
  "compare a db-record with the generated from fs"
  [db-record fs-record]
  (and (= (:chksum db-record) (:chksum fs-record))
       (= (:path db-record) (:path fs-record))))

(defn insert-entry
  "insert into db..."
  [path file]
  (let [now (msec)
        size (.length file)
        chksum (checksum file)
        took (- (msec) now)]
    (info "insert " chksum " " (grab-unit size) " " (ftime took) " " (str file))
    (try (insert-records :ic (create-record path file))
    (catch Exception e (error e)))))
 
(defn select-entries
  "all entries"
  []
  (with-connection db
    (with-query-results rs ["select * from ic"]
      (doall rs))))

(defn select-entry
  "look for entry with"
  [path]
  (with-connection db
    (with-query-results rs ["select * from ic where path=?" path]
      (first rs))))

(defn update-last-scan-for
  "update the filed lastscan for existing entry"
  [path]
  (info "re-check: " path)
  (with-connection db
    (update-values :ic ["path=?" path] {:lastscan (msec)})))

(defn update-entry
  "update an entry"
  [path file]
  (let [fs-record (create-record path file)
        db-record (select-entry (str file))]
    (if (records-equal? db-record fs-record)
      (update-last-scan-for (str file))
      (throw (Exception.
        (str "checksums differ! file:" (str file) \newline
             " db-entry: " (:chksum db-record) \newline
             " fs-entry: " (:chksum fs-record) \newline))))))
