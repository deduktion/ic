(ns ic.ui.gui
  (:use [ic.tools]
        [ic.config]
        [ic.stores]
        [ic.db]
        [ic.ic]
        [ic.stats]
        [clojure.tools.cli :only [cli]]
        [seesaw core]
        [clojure.tools.logging :only (info error)]
        [clj-logging-config.log4j]))
(set-logger! :pattern log-pattern)

(def table-header (list
  :path :chksum :algorithm
  :size :took
  :r :w :x :hidden))

(defn make-table [header data]
  (table :id :file-table
         :model [:columns header
                 :rows data]))

(defn table-data
  "create table data from entries"
  [entries]
  (map
    #(vector %1 %2 %3 %4 %5 %6 %7 %8 %9)
      (map :path entries)
      (map :chksum entries)
      (map :algorithm entries)
      (map grab-unit (map :size entries))
      (map ftime (map :took entries))
      (map :r entries)
      (map :w entries)
      (map :x entries)
      (map :hidden entries)))

(defn make-content [entries]
  (border-panel :border 5
                :hgap 5
                :vgap 5
      :north (label :id :current-dir
                    :text "menu")
      :west (label :id :file-tree
                   :text "tree" :visible? false)
      :center (scrollable
                (make-table table-header (table-data entries)))
      :east (label :id :file-props
                   :text "file-props"
                   :visible? false)
      :south (label :id :status
                    :text (str "Ready. " (str (total-stats))))))

(defn switch-to-gui
  "run in gui mode"
  [entries]
  (native!)
  (let [f (frame :title "IC"
                 :content (make-content entries)
                 :width 1200
                 :height 400
                 :visible? true
                 :on-close :exit)]
    (.setLocation f (java.awt.Point. 100 300))
    (timer (fn [e] (repaint! (select f [:#:clock])) 1000))))
