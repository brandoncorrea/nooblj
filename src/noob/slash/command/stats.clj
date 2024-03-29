(ns noob.slash.command.stats
  (:require [c3kit.apron.corec :as ccc]
            [discord.interaction :as interaction]
            [noob.core :as core]
            [noob.slash.core :as slash]
            [noob.style.core :as style]
            [noob.user :as user]))

(defn ability-score [attr loadout]
  (reduce + (ccc/map-some attr loadout)))

(defn describe [user]
  (let [loadout (user/loadout! user)]
    (core/join-lines
      (str "Niblets: " (:niblets user 0))
      (str "Level: " (user/level user))
      (str "Experience: " (:xp user 0))
      (str "⚔️ Attack: " (ability-score :attack loadout))
      (str "🛡 Defense: " (ability-score :defense loadout))
      (str "🥷 Stealth: " (ability-score :sneak loadout))
      (str "👁 Perception: " (ability-score :perception loadout)))))

(defmethod slash/handle-command "stats" [request]
  (let [user   (user/current request)
        member (:member request)]
    (interaction/embed! request
      {:title       "Stats"
       :description (describe user)
       :color       style/green
       :author      (user/->author member)})))
