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

(defn entries
  "all entries"
  []
  (with-connection db
    (with-query-results rs ["select * from ic"]
      (doall rs))))

(defn entry
  "look for entry with"
  [path]
  (with-connection db
    (with-query-results rs ["select * from ic where path=?" path]
      (doall rs))))
