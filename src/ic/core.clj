(ns ic.core
  (:use [clojure.java.jdbc :exclude (resultset-seq)]
        [digest] :reload-all
        [clojure.string :only (join)]
        [clojure.tools.cli :only [cli]]
        [clojure.tools.logging :only (info error)]
        [clojure.data.json :as json]
        [clj-logging-config.log4j])
  (:import java.io.File)
  (:gen-class :main true))

(set-logger! :pattern "%d - %m%n")
(info "ic start ")

;;;;; tools stuff
;;;;;
(def kb 1024)
(defn msec [] (System/currentTimeMillis))
(def units ["B " "kB" "MB" "GB" "TB" "PB" "EB" "ZB" "YB"])
(def units-size)
(defn ftime [ms] (format "%6.3fs" (/ (double ms) 1000)))
(defn exit [] (System/exit 0))
(defn blank [s] (clojure.string/blank? s))
(defn files [p] (file-seq (clojure.java.io/file p)))
(defn checksum [file]
  "sha-256, more later"
  (digest/sha-256 file))

(defn grab-unit
 "comment"
  [bytes]
  (let [sizes (iterate #(* %1 kb) 1)
        units-sizes (map #(vector %1 %2) units sizes)
        unit-size   (some #(if (< (/ bytes (second %)) kb) % false) units-sizes)]
    (format "%8.3f %s" (/ (double bytes) (second unit-size)) (first unit-size))))


;;;;; config stuff
;;;;;
(defn- user-prop
  "Returns the system property for user.<key>"
  [key]
  (System/getProperty (str "user." key)))
(def user-home (user-prop "home"))


(def config-path (str user-home "/.ic.config"))
(def db-path (str user-home "/.ic.db"))
(def default-store {:label "home" :path user-home})

(info "config-path:  " config-path)
(def default-config
  {:algorithm "sha-256"
   :interval 259200
   :stores
   [{:label "label1" :path "path1"}
    {:label "label2" :path "path2"}]})

(defn save-config [config]
  "save config"
  (info "save config: " config)
  (spit config-path (json/write-str default-config)))

(defn load-config []
  "load config"
  (info "load config: ")
  (json/read-str (slurp config-path)))

(defn init-config []
  "init config"
  (info "init config")
  (try (load-config)
    (catch java.io.FileNotFoundException e
      (do (info "no config present, create default-config")
          (save-config default-config)
          (load-config)))))

;;;;; db stuff
;;;;;
(def db {
    :classname "org.sqlite.JDBC"
    :subprotocol "sqlite"
    :subname db-path})

(defn create-db []
  (try (with-connection db
         (create-table :intchk
                       [:path :text]
                       [:chksum :text]
                       [:size :integer]
                       ;; todo:  owner, mode, group, etc
                       [:took :integer]
                       [:first :integer]
                       [:last :integer]))
    (catch Exception e (println e))))

(try (create-db)
 (catch Exception e (println e)))

(defn rescan-path [path]
  "rescan a collection"
  (info "rescan-path: " path)
  (doseq [f (files path)]
    (if (.isFile f)
      (let [now (msec)
            size (.length f)
            chksum (checksum f)
            took (- (msec) now)]
          (info chksum " " (grab-unit size) " " (ftime took) " " (str f))
          (try (insert-records
              :intchk
              {:path (str f) :chksum chksum :size size
               :took took    :first now     :last now})
          (catch Exception e (error e)))))))

(defn rescan [label path]
  "rescan via name, load path if exists else add"
  (info "rescan: " label " " path)
  (rescan-path path)
  (exit))

(defn add-store [label path]
  "add a new store"
  (info "add a new store " label " " path))

(defn list-stores []
  "list all stores or just with given name"
  (println "show all stores...")
  (exit))

(defn show-banner [banner]
  "show banner and exit"
  (println banner)
  (exit))


(defn -main [& args]
  "int-check in clojure"
  (let [[options args banner] 
      (cli args
         ["-h" "--help" "show help"]
         ["-s" "--store" "store"]
         ["-S" "--scan" "scan store"]
         ["-a" "--algorithm" "set cryptographic hash type" :default "sha-256"]
         ["-l" "--list" "list stores" :flag true]
         ["-r" "--rescan" "rescan store" :flag false]
         ["-p" "--path"
          "path to a directory with your files to index"
          :default ""]
         ["-l" "--label"
          "label for your collection"
          :default ""])]
    (init-config)
    (with-connection db
      (if (contains? options :help) (show-banner banner))
      (if (and
            (contains? options :label)
            (contains? options :path))
        (rescan (:label options) (:path options)))
      (if (and
            (contains? options :rescan)
            (contains? options :label))
        (rescan (:label options) (:path options)))
      (if (contains? options :list) (list-stores)))))
