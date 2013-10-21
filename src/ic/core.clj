(ns ic.core
  (:use [ic db tools]
        [ic.ui console gui]
        [clojure.java.jdbc :only (with-connection)]
        [clojure.tools.logging :only (info)]
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
      (if (contains? options :gui)
        (switch-to-gui (select-entries))
        (switch-to-terminal options banner)))))
