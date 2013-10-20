(ns ic.core
  (:use [ic.tools]
        [ic.config]
        [ic.stores]
        [ic.db]
        [ic.ic]
        [ic.stats]
        [clojure.java.jdbc :exclude (resultset-seq)]
        [clojure.tools.cli :only [cli]]
        [seesaw core]
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

(defn make-content [entries]
  (border-panel :border 5 :hgap 5 :vgap 5
      :north (label :id :current-dir
                    :text "menu")
      :west (label :id :file-tree
                   :text "tree" :visible? false)
      :center (scrollable (listbox :model (map :path entries)))
      :east (label :id :file-props
                   :text "file-props"
                   :visible? false)
      :south (label :id :status
                    :text (str "Ready. " (str (total-stats))))))

(defn switch-to-gui
  "run in gui mode"
  [entries]
  (let [f (frame :title "IC"
                 :content (make-content entries)
                 :width 1200
                 :height 400
                 :visible? true
                 :on-close :exit)]
    (.setLocation f (java.awt.Point. 100 300))
    (timer (fn [e] (repaint! (select f [:#:clock])) 1000))))

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
      (if (contains? options :gui)
        (do (native!)
            (switch-to-gui (select-entries)))
        (switch-to-terminal options banner)))))
