(ns tailrecursion.hiprepl
  (:require [clojail.core      :refer [sandbox safe-read]]
            [clojail.testers   :refer [secure-tester]])
  (:import
   [org.jivesoftware.smack ConnectionConfiguration XMPPConnection XMPPException PacketListener]
   [org.jivesoftware.smack.packet Message Presence Presence$Type]
   [org.jivesoftware.smack.filter MessageTypeFilter]
   [org.jivesoftware.smackx.muc MultiUserChat]))

(def available-presence (Presence. Presence$Type/available))

(defn packet-listener [conn processor]
  (proxy
      [PacketListener]
      []
    (processPacket [packet] (processor conn packet))))

(defn error->map [e]
  (if (nil? e)
    nil
    {:code (.getCode e) :message (.getMessage e)}))

(defn message->map [#^Message m]
  (try
   {:body (.getBody m)}
   (catch Exception e (println e) {})))

(defn with-message-map [handler]
  (fn [muc packet]
    (let [message (message->map #^Message packet)]
      (try
       (handler muc message)
       (catch Exception e (println e))))))

(defn wrap-responder [handler]
  (fn [muc message]
    (if-let [resp (handler message)]
      (.sendMessage muc resp))))

(defn connect
  [username password]
  (let [conn (XMPPConnection. (ConnectionConfiguration. "chat.hipchat.com" 5222))]
    (.connect conn)
    (try
      (.login conn username password)
      (catch XMPPException e
        (throw (Exception. "Couldn't log in with user's credentials."))))
    (.sendPacket conn available-presence)
    conn))

(defn join
  [conn room room-nickname handler]
  (let [muc (MultiUserChat. conn (str room "@conf.hipchat.com"))]
    (.join muc room-nickname)
    (.addMessageListener muc
                         (packet-listener muc (with-message-map (wrap-responder handler))))
    conn))

(def secure-sandbox (sandbox secure-tester))

(defn eval-handler
  [{:keys [body] :as msg}]
  (when (.startsWith body ",")
    (try
      (binding [*print-length* 30]
        (pr-str (secure-sandbox (safe-read (.substring body 1)))))
      (catch Throwable t
        (.getMessage t)))))

(defn -main
  [config-path]
  (let [{:keys [username password rooms room-nickname]} (binding [*read-eval* false]
                                                          (read-string (slurp config-path)))
        conn (connect username password)]
    (doseq [room rooms]
      (join conn room room-nickname eval-handler))
    @(promise)))