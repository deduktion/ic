(ns ic.db
  (:use [ic.tools]
        [ic.config]
        [clojure.java.jdbc :exclude (resultset-seq)]
        [clojure.tools.logging :only (info error)]
        [clj-logging-config.log4j]))
(set-logger! :pattern "%d - %m%n")

(def db {
    :classname "org.sqlite.JDBC"
    :subprotocol "sqlite"
    :subname db-path})

(defn create-db []
  (try (with-connection db
         (create-table :intchk
                       [:path :text]
                       [:chksum :text]
                       [:size :integer]
                       ;; todo:  owner, mode, group, etc
                       [:took :integer]
                       [:first :integer]
                       [:last :integer]))
    (catch Exception e (println e))))

(try (create-db)
 (catch Exception e (println e)))
