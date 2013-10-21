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
            [:algorithm :text]
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
  [path file algorithm]
  (let [now (msec)
        size (.length file)
        chksum (checksum file algorithm)
        took (- (msec) now)
        filepath (str file)
        last-modified (.lastModified file)
        r (.canRead file)
        w (.canWrite file)
        x (.canExecute file)
        hidden (.isHidden file)]
    {:prefix path :path filepath :chksum chksum
     :algorithm algorithm :size size :took took
     :firstscan now :lastscan now
     :lastmodified last-modified
     :r r :w w :x x :hidden hidden}))

(defn records-equal?
  "compare a db-record with the generated from fs"
  [db-record fs-record]
  (every? true? (list (= (:chksum db-record) (:chksum fs-record))
                      (= (:path db-record) (:path fs-record))
                      (= (:algorithm db-record) (:algorithm fs-record)))))

(defn insert-entry
  "insert into db..."
  [path file algorithm]
  (let [now (msec)
        size (.length file)
        chksum (checksum file algorithm)
        took (- (msec) now)]
    (info "insert " algorithm
          " " chksum
          " " (grab-unit size)
          " " (ftime took)
          " " (str file))
    (try (insert-records :ic (create-record path file algorithm))
    (catch Exception e (error e)))))
 
(defn select-entries
  "all entries"
  []
  (with-query-results rs ["select * from ic"] (doall rs)))

(defn select-entry
  "look for entry with"
  [path algorithm]
  (with-query-results rs
    ["select * from ic where path=? and algorithm=?" path algorithm]
    (first rs)))

(defn update-last-scan-for
  "update the filed lastscan for existing entry"
  [path algorithm]
  (info "re-check: [" algorithm "]" path)
  (update-values
    :ic ["path=? and algorithm=?" path algorithm]
    {:lastscan (msec)}))

(defn update-entry
  "update an entry"
  [path file algorithm]
  (let [fs-record (create-record path file algorithm)
        db-record (select-entry (str file) algorithm)]
    (if (records-equal? db-record fs-record)
      (update-last-scan-for (str file) algorithm)
      (throw (Exception.
        (str "checksums differ! file:" (str file) \newline
             " db-entry: " (:chksum db-record) \newline
             " fs-entry: " (:chksum fs-record) \newline))))))
