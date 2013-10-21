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
(def username (user-prop "name"))
(def config-path (str user-home "/.ic.config"))
(def db-path (str user-home "/.ic.db"))
(def store-path (str user-home "/.ic.stores"))
(def one-month-in-seconds 2592000)
(def default-algorithm "sha-256")
(def default-config {:algorithm default-algorithm
                     :interval one-month-in-seconds})

(defn save-config
  "save config"
  [config]
  (info "save config")
  (spit config-path (json/write-str config)))

(defn load-config
  "load config"
  []
  (info "load config")
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

(defn set-algorithm
  "set algorithm for ic"
  [algorithm]
  (info "set algorithm: " algorithm)
  (let [config (init-config)]
    (save-config (assoc config "algorithm" algorithm))))

(defn set-interval
  "set interval for next scan"
  [interval]
  (info "set interval: " interval)
  (let [config (init-config)]
    (save-config (assoc config "interval" (str>int interval)))))
