(ns ic.core
  (:use 
        [ic.config]
        [ic.db]
        [ic.ui]
        [ic.tools]
        [clojure.java.jdbc :exclude (resultset-seq)]
        [clojure.tools.logging :only (info error)]
        [clj-logging-config.log4j])
  (:import java.io.File)
  (:gen-class :main true))
(set-logger! :pattern log-pattern)
(info "ic start " (msec))

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
    (with-connection db
      (init-config)
      (if (contains? options :gui)
        (switch-to-gui (select-entries))
        (switch-to-terminal options banner)))))
