(ns hiprepl.core
  (:require [clojure.data.json :as json]
            [clj-http.client :as client]
            [clojure.set :as s]
            [overtone.at-at :refer [mk-pool every]])
  (:import java.util.concurrent.ScheduledThreadPoolExecutor))

(def api-url "http://api.hipchat.com/v1")

(defn rooms
  [token]
  (let [query {:auth_token token :format "json"}]
    (-> (client/get (str api-url "/rooms/list") {:query-params query})
        :body
        json/read-str
        (get "rooms"))))

(defn room-id
  [token room-name]
  (let [rooms (rooms token)]
    (get (first (filter #(= room-name (get % "name")) rooms)) "room_id")))

(defn fetch-recent
  [token room-name]
  (let [query {:room_id (room-id token room-name)
               :auth_token token
               :date "recent"
               :format "json"}]
    (-> (client/get (str api-url "/rooms/history") {:query-params query})
        :body
        json/read-str
        (get "messages")
        set)))

(defn user-info
  [token user-id]
  (let [query {:user_id user-id
               :auth_token token}]
    (-> (client/get (str api-url "/users/show") {:query-params query})
        :body
        json/read-str
        (get "user"))))

(defn message
  [token room-name msg]
  (let [query {:room_id (room-id token room-name)
               :from (str "Clojure " (clojure-version))
               :message msg
               :auth_token token
               :format "json"}]
    (-> (client/get (str api-url "/rooms/message") {:query-params query})
        :body
        json/read-str)))

(defn eval-messages
  [token room-name messages]
  (println "Evaluating messages.")
  (doseq [{:strs [message from]} messages
          :when (.startsWith message ",")
          :let [code (read-string (.substring message 1))
                return (pr-str (try (eval code) (catch Throwable t (.getMessage t))))
                mention (get (user-info token (get from "user_id")) "mention_name")]]
    (println (format "%s ran %s, got %s" mention (pr-str code) return))
    (future (message token room-name (format "@%s %s" mention return)))))

(defn -main
  [auth-token room-name]
  (let [messages (agent {:prev (fetch-recent auth-token room-name)
                         :eval #{}})
        pool (mk-pool)]
    (add-watch messages ::eval #(eval-messages auth-token room-name (:eval %4)))
    (every 5000
           (fn []
             (println "Polling...")
             (send-off messages (fn [{:keys [prev]}]
                                  (let [new (fetch-recent auth-token room-name)]
                                    {:prev new
                                     :eval (s/difference new prev)}))))
           pool)))