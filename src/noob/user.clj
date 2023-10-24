(ns noob.user
  (:require [c3kit.apron.corec :as ccc]
            [c3kit.bucket.api :as db]
            [noob.roll :as roll]))

(defn by-discord-id [id] (db/ffind-by :user :discord-id id))
(defn discord-id [request] (-> request :member :user :id))
(defn current [request] (-> request discord-id by-discord-id))
(defn mention [user] (str "<@" (:discord-id user user) \>))
(defn create [discord-id] {:kind :user :discord-id discord-id})
(defn create! [discord-id] (db/tx (create discord-id)))
(defn find-or-create [discord-id]
  (or (by-discord-id discord-id)
      (create discord-id)))

(defn update-niblets [f user amount]
  (if (:niblets user)
    (update user :niblets f amount)
    (assoc user :niblets (f amount))))

(defn deposit-niblets [user amount] (update-niblets + user amount))
(defn withdraw-niblets [user amount] (update-niblets - user amount))

(defn transfer-niblets! [from to amount]
  (db/tx* [(withdraw-niblets from amount)
           (deposit-niblets to amount)]))

(defn level [user]
  (condp > (:xp user 0)
    100 1
    250 2
    450 3
    700 4
    1000 5
    1350 6
    1750 7
    2200 8
    2700 9
    10))

(defn niblets [user] (:niblets user 0))
(defn owns? [user item] (some (partial = (:id item)) (:inventory user)))
(defn unslotted? [user slot]
  (not-any? #(-> % db/entity :slot (= slot)) (:loadout user)))

(defn equip [user item] (update user :loadout conj (:id item)))

(defn ability-score [user ability]
  (->> (:loadout user)
       (ccc/map-some (comp ability db/entity))
       (reduce +)))

(defn roll [user ability]
  (roll/ability (level user) (ability-score user ability)))

(defn gain-xp [user base-factor challenge-level]
  (let [reward (roll/xp-reward (level user) base-factor challenge-level)
        xp     (+ (:xp user 0) reward)]
    (assoc user :xp xp)))

(defn gain-xp! [user base-factor challenge-level]
  (db/tx (gain-xp user base-factor challenge-level)))

(defn purchase! [user item]
  (-> user
      (update :inventory conj (:id item))
      (withdraw-niblets (:price item))
      db/tx))

(defn display-name [member-or-user]
  (or (:nick member-or-user)
      (:global-name member-or-user)
      (-> member-or-user :user :global-name)))

(defn avatar [member-or-user]
  (let [user-id (or (:id member-or-user)
                    (-> member-or-user :user :id))
        avatar  (or (-> member-or-user :avatar)
                    (-> member-or-user :user :avatar))]
    (when (and user-id avatar)
      (str "https://cdn.discordapp.com/avatars/" user-id "/" avatar ".png"))))

(defn ->author [member-or-user]
  {:name     (display-name member-or-user)
   :icon_url (avatar member-or-user)})
