(ns ic.ui.gui
  (:use [ic tools stats]
        [seesaw core tree]
        [clojure.tools.logging :only (info error)]
        [clj-logging-config.log4j])
  (:import [java.io.File]))
(set-logger! :pattern log-pattern)

(def border-with 1)

(def table-header (list
  :path :chksum :algorithm
  :size :took
  :r :w :x
  :hidden))

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

(defn make-table [entries]
  (scrollable
    (table :id :file-table
           :model [:columns table-header
                   :rows (table-data entries)])))

(defn file-exit [e] (System/exit 0))

(def menus
  (let [file-exit (action :handler file-exit
                          :name "exit"
                          :tip "exit ic")]
    (menubar :items [(menu :text "file"
                           :items [file-exit])
                     (menu :text "help"
                           :items [])])))
  
(def file-detail-panel
  (text :id :file-details
        :text "file details"
        :multi-line? false
        :editable? false))

(def statusbar-panel
  (text :id :statusbar-panel
        :text "TODO: status bar"))

(def tree-model
  (simple-tree-model
    #(.isDirectory %)
    (fn [f] (filter #(.isDirectory %) (.listFiles f)))
    (java.io.File. ".")))

(def tree-panel
  (scrollable
    (tree :id :tree-view
          :model tree-model)))

(defn make-content [entries]
  (border-panel
    :border border-with
    :hgap border-with
    :vgap border-with
    :west tree-panel
    :center (make-table entries)
    :east file-detail-panel
    :south statusbar-panel))

(defn make-main-frame
  "create the mail windows"
  [entries]
  (frame
    :title "IC"
    :menubar menus
    :content (make-content entries)
    :width 1200
    :height 400
    :minimum-size [640 :by 480]
    :visible? true
    :on-close :exit))

(defn switch-to-gui
  "run in gui mode"
  [entries]
  (native!)
  (let [f (make-main-frame entries)]
    (.setLocation f (java.awt.Point. 100 300))
    (timer (fn [e] (repaint! (select f [:#:clock])) 1000))))
