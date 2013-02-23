(defproject tailrecursion/hiprepl "1.0.0-SNAPSHOT"
  :description "Clojure REPL for HipChat"
  :url "https://github.com/tailrecursion/hiprepl"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [org.clojure/data.json "0.2.1"]
                 [clj-http "0.6.4"]
                 [overtone/at-at "1.1.1"]
                 [clojail "1.0.4"]]
  :main tailrecursion.hiprepl)