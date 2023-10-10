(ns noob.spec-helper
  (:require [c3kit.apron.time :as time]
            [discljord.connections :as discord-ws]
            [discljord.messaging :as discord-rest]
            [discord.interaction :as interaction]
            [noob.bot :as bot]
            [speclj.core :refer :all]))

(defn stub-discord []
  (redefs-around [discord-ws/status-update!      (stub :discord/status-update!)
                  interaction/reply!             (stub :discord/reply-interaction!)
                  interaction/reply-ephemeral!   (stub :discord/reply-interaction-ephemeral!)
                  discord-rest/create-message!   (stub :discord/create-message!)
                  discord-rest/get-current-user! (stub :discord/get-current-user!)]))

(defn stub-bot []
  (redefs-around [bot/gateway         (constantly :bot/gateway)
                  bot/events          (constantly :bot/events)
                  bot/rest-connection (constantly :bot/rest)
                  bot/init!           (stub :bot/init!)
                  bot/stop!           (stub :bot/stop!)]))

(defn stub-now [time]
  (redefs-around [time/now (stub :now {:return time})]))

(defmacro should-have-replied [request message]
  `(should-have-invoked :discord/reply-interaction! {:with [~request ~message]}))

(defmacro should-have-replied-ephemeral [request message]
  `(should-have-invoked :discord/reply-interaction-ephemeral! {:with [~request ~message]}))
