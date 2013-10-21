(ns ic.ui
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

(defn arguments
  "specify arguments"
  [args]
  (cli args
      ["-h" "--help" "show help"]
      ["-i" "--info" "info and stats"]
      ["-R" "--rescan-all" "rescan all stores"]
      ["-r" "--rescan" "rescan store"]
      ["-g" "--gui" "start the gui"]
      ["-a" "--algorithm" "set cryptographic hash type: md5, sha-1, sha-256, sha-512"]
      ["-c" "--clock" "set interval for next scan  default: 2592000 (1 month)"]
      ["-s" "--store" "store"]
        ["-n" "--new" "new store"]
        ["-d" "--delete" "delete a store"]
        ["-l" "--list" "list stores"]
        ["-p" "--path" "path to a directory with your files to index"]))

(defn switch-to-terminal
  "run in terminal"
  [options banner]
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
  (if (contains? options :list) (list-stores)))
