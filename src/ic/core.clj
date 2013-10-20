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
      ["-h" "--help"
       "show help"]
      ["-i" "--info"
       "info and stats"]
      ["-R" "--rescan-all"
       "rescan all stores"]
      ["-r" "--rescan"
       "rescan store"
       :flag false]
      ["-a" "--algorithm"
       "set cryptographic hash type: md5, sha-1, sha-256, sha-512"]
      ["-c" "--clock"
       "set interval for next scan  default: 2592000 (1 month)"]
      ["-s" "--store"
       "store"]
        ["-n" "--new"
         "new store"
         :flag false]
        ["-d" "--delete"
         "delete a store"
         :flag false]
        ["-l" "--list"
         "list stores"
         :flag false]
        ["-p" "--path"
         "path to a directory with your files to index"]))

(defn -main
  "int-check in clojure
   so far a basic first example:

    - set algorithm
      lein run -a md5
      lein run -a sha-1
      lein run -a sha-256
      lein run -a sha-512

    - set interval for next scan in seconds
      lein run -c 30   ;;(rescan after 30 seconds)
      lein run -c 2592000  ;;(rescan after 1 month)

    - add new store
      lein run -s storename -p /path/to/store -n
      lein run -s otherstore -p /path/to/other/store -n

    - list stores
      lein run -l

    - rescan store by name
      lein run -s storename -r

    - rescan all stores
      lein run -R

    - show basic stats
      lein run -i
  "
  [& args]
  (let [[options args banner] (arguments args)]
    (init-config)
    (with-connection db
      (if (contains? options :help) (println banner))
      (if (contains? options :info) (total-stats))
      (if (contains? options :rescan-all) (rescan-all))
      (if (contains? options :algorithm) (set-algorithm (:algorithm options)))
      (if (contains? options :clock) (set-interval (:clock options)))
      (if (contains? options :store)
        (do
          (if (contains? options :path)
            (do (if (contains? options :new)
                  (add-store (:store options) (:path options)))
                (if (contains? options :delete)
                  (delete-store (:store options))))
          (if (and (contains? options :rescan)
                   (store-exists? (:store options)))
            (rescan (:store options))))
        (list-stores)))
      (if (contains? options :list) (list-stores)))))
