(ns noob.slash.command.attack-spec
  (:require [c3kit.bucket.api :as db]
            [noob.bogus :as bogus :refer [bill ted]]
            [noob.roll :as roll]
            [noob.slash.command.attack :as sut]
            [noob.slash.core :as slash]
            [noob.spec-helper :as spec-helper :refer [should-have-created-message should-have-replied]]
            [noob.user :as user]
            [speclj.core :refer :all]))

(def request)

(describe "Attack"
  (with-stubs)
  (spec-helper/stub-discord)
  (bogus/with-kinds :all)

  (redefs-around [sut/success-messages ["%1$s attacks %2$s"]
                  sut/fail-messages    ["%1$s fails to attack %2$s"]
                  sut/self-messages    ["%s attacks themselves"]])

  (context "attacking people"

    (with request {:data   {:name     "attack"
                            :resolved {:members {(:discord-id @ted) {:nick "teddy"}}}
                            :options  {:target (:discord-id @ted)}}
                   :member {:nick "billy"
                            :user {:id (:discord-id @bill)}}})

    (it "attacks self"
      (let [request (assoc-in @request [:data :options :target] (:discord-id @bill))]
        (slash/handle-command request)
        (should-have-replied request "billy attacks themselves")))

    (it "fails attack"
      (with-redefs [user/roll (fn [_user ability] (if (= :defense ability) 1 0))]
        (slash/handle-command @request)
        (should-have-replied @request "billy fails to attack teddy")
        (should= 100 (:xp @bill))
        (should= (roll/xp-reward 1 15 2) (:xp @ted))))

    (it "succeeds attack"
      (with-redefs [user/roll (fn [_user ability] (if (= :attack ability) 1 0))]
        (slash/handle-command @request)
        (should-have-replied @request "billy attacks teddy")
        (should= (+ 100 (roll/xp-reward 2 25 1)) (:xp @bill))
        (should-be-nil (:xp @ted))))

    (it "winner levels up"
      (with-redefs [user/roll (fn [_user ability] (if (= :attack ability) 1 0))]
        (db/tx @bill :xp 99)
        (slash/handle-command @request)
        (should-have-created-message @request (str (user/mention @bill) " has reached level 2!"))))

    (it "target wins on ties"
      (with-redefs [user/roll (fn [_user ability] (if (= :attack ability) 1 0.75))]
        (slash/handle-command @request)
        (should-have-replied @request "billy fails to attack teddy")))
    )
  )
