(ns noob.spec-helper
  (:require [c3kit.apron.time :as time]
            [discljord.connections :as discord-ws]
            [discljord.messaging :as discord-rest]
            [discord.api :as discord-api]
            [noob.bot :as bot]
            [speclj.core :refer :all]))

(defn stub-discord []
  (around [it]
    (with-redefs [discord-ws/status-update!      (stub :discord/status-update!)
                  discord-api/reply-interaction! (stub :discord/reply-interaction!)
                  discord-rest/create-message!   (stub :discord/create-message!)
                  discord-rest/get-current-user! (stub :discord/get-current-user!)]
      (it))))

(defn stub-bot []
  (around [it]
    (with-redefs [bot/gateway         (constantly :bot/gateway)
                  bot/events          (constantly :bot/events)
                  bot/rest-connection (constantly :bot/rest)
                  bot/init!           (stub :bot/init!)
                  bot/stop!           (stub :bot/stop!)]
      (it))))

(defn stub-now [time]
  (around [it]
    (with-redefs [time/now (stub :now {:return time})]
      (it))))
