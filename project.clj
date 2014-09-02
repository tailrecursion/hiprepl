(defproject tailrecursion/hiprepl "1.0.0-SNAPSHOT"
  :description "Clojure REPL for HipChat"
  :url "https://github.com/tailrecursion/hiprepl"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0-alpha1"]
                 [clojail "1.0.4"]
                 [jivesoftware/smack "3.1.0"]
                 [jivesoftware/smackx "3.1.0"]]
  :profiles {:uberjar {:aot :all}}
  :target-path "target/%s/"
  :main tailrecursion.hiprepl)
