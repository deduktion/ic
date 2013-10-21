(defproject ic "0.1.0-SNAPSHOT"
  :description "ic"
  :url "https://github.com/thrstnh/ic"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [digest "1.4.3"]
                 [korma "0.3.0-RC5"]
                 [clj-time "0.6.0"]
                 [org.xerial/sqlite-jdbc "3.7.2"]
                 [org.clojure/tools.cli "0.2.4"]
                 [org.clojure/tools.logging "0.2.6"]
                 [org.clojure/data.json "0.2.3"]
                 [seesaw "1.4.4"]
                 [clj-time "0.6.0"]
                 [clj-logging-config "1.9.10"]]
  :main ^:skip-aot ic.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
