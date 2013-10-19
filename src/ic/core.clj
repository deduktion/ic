(ns ic.core
  (:use [ic.tools]
        [ic.config]
        [ic.db]
        [ic.ic]
        [clojure.java.jdbc :exclude (resultset-seq)]
        [clojure.tools.cli :only [cli]]
        [clojure.tools.logging :only (info error)]
        [clj-logging-config.log4j])
  (:import java.io.File)
  (:gen-class :main true))
(set-logger! :pattern log-pattern)
(info "ic start " (msec))

(defn -main [& args]
  "int-check in clojure"
  (let [[options args banner] 
      (cli args
         ["-h" "--help" "show help"]
         ["-r" "--rescan" "rescan store" :flag false]
         ["-a" "--algorithm" "set cryptographic hash type" :default "sha-256"]
         ["-s" "--store" "store"]
           ["-n" "--new" "new store" :flag false]
           ["-d" "--delete" "delete a store" :flag false]
           ["-l" "--list" "list stores" :flag false]
           ["-p" "--path" "path to a directory with your files to index"])]
    (init-config)
    (with-connection db
      (if (contains? options :help) (show-banner banner))
      (if (contains? options :store)
        (do
          (if (contains? options :path)
            (do (if (contains? options :new)
                  (add-store (:store options) (:path options)))
                (if (contains? options :delete)
                  (delete-store (:store options))))
          (if (and
                (contains? options :rescan)
                (store-exists? (:store options)))
            (rescan (:store options))))
        (list-stores)))
      (if (contains? options :list) (list-stores)))))
