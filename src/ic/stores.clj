(ns ic.stores
  (:use [ic.tools]
        [ic.config]
        [clojure.tools.logging :only (info error)]
        [clojure.data.json :as json]
        [clj-logging-config.log4j])
  (:require
        [clojure.pprint :only (pprint) :as pp]))
(set-logger! :pattern log-pattern)

(defn save-stores
  "save a store"
  [stores]
  (info "save stores " stores)
  (spit store-path (json/write-str stores)))

(defn load-stores
  "load stores"
  []
  (info "load stores")
  (try (json/read-str (slurp store-path))
  (catch java.io.FileNotFoundException e
    (do (error e)
        (save-stores {})))
  (catch Exception e (error e))))

(defn list-stores
  "list all stores"
  []
  (info "list all stores")
  (pp/pprint (load-stores)))

(defn add-store
  "add a store"
  [name path]
  (info "add a store: " name " " path)
  (let [stores (load-stores)]
    (save-stores (assoc stores name path))))

(defn delete-store
  "delete a store"
  [name]
  (info "delete store: " name)
  (let [stores (load-stores)]
    (save-stores (dissoc stores name))))

(defn store-exists?
  "check if a store exists"
  [name]
  (info "store exists? " name)
  (contains? (load-stores) name))
