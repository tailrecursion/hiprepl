(ns tailrecursion.hiprepl
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [clojail.core    :refer [sandbox safe-read]]
            [clojail.testers :refer [secure-tester]])
  (:import
   [java.io StringWriter]
   [org.jivesoftware.smack ConnectionConfiguration XMPPConnection XMPPException PacketListener]
   [org.jivesoftware.smack.packet Message Presence Presence$Type]
   [org.jivesoftware.smackx.muc MultiUserChat])
  (:gen-class))

(defn packet-listener [conn processor]
  (reify PacketListener
    (processPacket [_ packet]
      (processor conn packet))))

(defn message->map [#^Message m]
  (try
    {:body (.getBody m)
     :from (.getFrom m)}
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
  [username password resource]
  (let [conn (XMPPConnection. (ConnectionConfiguration. "chat.hipchat.com" 5222))]
    (.connect conn)
    (try
      (.login conn username password resource)
      (catch XMPPException e
        (throw (Exception. "Couldn't log in with user's credentials."))))
    (.sendPacket conn (Presence. Presence$Type/available))
    conn))

(defn join
  [conn room room-nickname handler]
  (let [muc (MultiUserChat. conn (str room "@conf.hipchat.com"))]
    (.join muc room-nickname)
    (.addMessageListener muc
                         (packet-listener muc (with-message-map (wrap-responder handler))))
    muc))

(defn make-safe-eval
  [{sandbox-config :sandbox}]
  (let [our-sandbox (sandbox (deref (find-var sandbox-config)))]
    (fn [form bindings]
      (our-sandbox `(pr ~form)
                   (merge {#'*print-length* 30}
                          bindings)))))

(defn message-handler
  [{nickname :room-nickname :as config}]
  (let [safe-eval (make-safe-eval config)]
    (fn [{:keys [body from]}]
      (when (and (not= nickname
                       (string/replace (or from "") #"^[^/]*/" ""))
                 (.startsWith body ","))
        (try
          (let [output (StringWriter.)]
            (safe-eval (safe-read (.substring body 1))
                       {#'*out* output
                        #'*err* output})
            (.toString output))
          (catch Throwable t
            (.getMessage t)))))))

(defn -main
  []
  (let [{:keys [username password rooms room-nickname] :as config} (safe-read (slurp (io/resource "config.clj")))
        conn (connect username password "bot")]
    (doseq [room rooms]
      (join conn room room-nickname (message-handler config)))
    @(promise)))
