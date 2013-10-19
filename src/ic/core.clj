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
         ["-s" "--store" "store"]
         ["-S" "--scan" "scan store"]
         ["-a" "--algorithm" "set cryptographic hash type" :default "sha-256"]
         ["-l" "--list" "list stores" :flag true]
         ["-r" "--rescan" "rescan store" :flag false]
         ["-p" "--path"
          "path to a directory with your files to index"
          :default ""]
         ["-l" "--label"
          "label for your collection"
          :default ""])]
    (init-config)
    (with-connection db
      (if (contains? options :help) (show-banner banner))
      (if (and
            (contains? options :label)
            (contains? options :path))
        (rescan (:label options) (:path options)))
      (if (and
            (contains? options :rescan)
            (contains? options :label))
        (rescan (:label options) (:path options)))
      (if (contains? options :list) (list-stores)))))
