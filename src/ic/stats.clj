(ns ic.stats
  (:use [clojure.tools.logging :only (info error)]
        [ic.tools]
        [ic.config]
        [ic.db]
        [clj-logging-config.log4j]))
(set-logger! :pattern log-pattern)

(defn stats
  "show stats of store with name"
  []
  (let [entries (entries)
        size-total (reduce + (map :size entries))]
    (println "STATS: size: " (grab-unit size-total))))
