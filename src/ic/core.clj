(ns ic.core
  (:use [ic.config]
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
  "run ic"
  [& args]
  (let [[options args banner] (arguments args)]
    (with-connection db
      (init-config)
      (if (contains? options :gui)
        (switch-to-gui (select-entries))
        (switch-to-terminal options banner)))))
