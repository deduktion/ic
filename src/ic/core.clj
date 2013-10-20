(ns ic.core
  (:use [ic.tools]
        [ic.config]
        [ic.stores]
        [ic.db]
        [ic.ic]
        [ic.stats]
        [clojure.java.jdbc :exclude (resultset-seq)]
        [clojure.tools.cli :only [cli]]
        [clojure.tools.logging :only (info error)]
        [clj-logging-config.log4j])
  (:import java.io.File)
  (:gen-class :main true))
(set-logger! :pattern log-pattern)
(info "ic start " (msec))

(defn arguments
  "specify arguments"
  [args]
  (cli args
      ["-h" "--help" "show help"]
      ["-i" "--info" "info and stats"]
      ["-r" "--rescan" "rescan store" :flag false]
      ["-a" "--algorithm" "set cryptographic hash type" :default "sha-256"]
      ["-s" "--store" "store"]
        ["-n" "--new" "new store" :flag false]
        ["-d" "--delete" "delete a store" :flag false]
        ["-l" "--list" "list stores" :flag false]
        ["-p" "--path" "path to a directory with your files to index"]))

(defn -main
  "int-check in clojure
   so far a basic first example:
    - add new store
      lein run -s storename -p /path/to/store -n
    - list stores
      lein run -l
    - rescan store by name
      lein run -s storename -r
    - show basic stats
      lein run -i
  "
  [& args]
  (let [[options args banner] (arguments args)]
    (init-config)
    (with-connection db
      (if (contains? options :help) (println banner))
      (if (contains? options :info) (println (stats)))
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
