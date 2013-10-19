(ns ic.config
  (:use [ic.tools]
        [clojure.tools.logging :only (info error)]
        [clojure.data.json :as json]
        [clj-logging-config.log4j]))
(set-logger! :pattern log-pattern)

(defn- user-prop
  "Returns the system property for user.<key>"
  [key]
  (System/getProperty (str "user." key)))
(def user-home (user-prop "home"))

(def config-path (str user-home "/.ic.config"))
(def db-path (str user-home "/.ic.db"))
(def store-path (str user-home "/.ic.stores"))
(def default-config {:algorithm "sha-256" :interval 259200})

(info "config-path:  " config-path)

(defn save-config
  "save config"
  [config]
  (info "save config: " config)
  (spit config-path (json/write-str default-config)))

(defn load-config
  "load config"
  []
  (info "load config: ")
  (json/read-str (slurp config-path)))

(defn init-config
  "init config"
  []
  (info "init config")
  (try (load-config)
    (catch java.io.FileNotFoundException e
      (do (info "no config present, create default-config")
          (save-config default-config)
          (load-config)))))
