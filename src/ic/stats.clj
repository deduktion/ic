(ns ic.stats
  (:use [clojure.tools.logging :only (info error)]
        [ic.tools]
        [ic.config]
        [ic.db]
        [clj-logging-config.log4j]))
(set-logger! :pattern log-pattern)

(defn total-size "" [entries] (reduce + (map :size entries)))
(defn total-took "" [entries] (reduce + (map :took entries)))
(defn total-hidden "" [entries] (count (filter pos? (map :hidden entries))))
(defn total-read "" [entries] (count (filter pos? (map :r entries))))
(defn total-write "" [entries] (count (filter pos? (map :w entries))))
(defn total-execute "" [entries] (count (filter pos? (map :x entries))))
(defn total-stats
  "show stats of store with name"
  []
  (let [entries (select-entries)
        stats {:files (count entries)
               :size (grab-unit (total-size entries))
               :scantime-hr (htime (total-took entries))
               :scantime-sec (ftime (total-took entries))
               :hidden (total-hidden entries)
               :r (total-read entries)
               :w (total-write entries)
               :x (total-execute entries)}]
    (println "STATS\n" stats)))
