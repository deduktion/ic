(ns ic.db
  (:use [ic.tools]
        [ic.config]
        [clojure.pprint]
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
  (try (with-connection db
         (create-table :ic
                       [:path :text]
                       [:chksum :text]
                       [:size :integer]
                       ;; todo:  owner, mode, group, etc
                       [:took :integer]
                       [:first :integer]
                       [:last :integer]))
    (catch Exception e (println e))))

(try (create-db)
 (catch java.sql.BatchUpdateException e (error e))
 (catch Exception e (error e)))

(defn insert-entry
  "insert into db..."
  [f]
  (let [now (msec)
        size (.length f)
        chksum (checksum f)
        took (- (msec) now)]
    (info "insert " chksum " " (grab-unit size) " " (ftime took) " " (str f))
    (try (insert-records :ic
        {:path (str f) :chksum chksum :size size
         :took took    :first now     :last now})
    (catch Exception e (error e)))))

(defn update-entry
  "update an entry"
  [path]
  (info "TODO: update an entry: " path))

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
