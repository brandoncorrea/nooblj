(ns noob.slash.inventory-spec
  (:require [c3kit.bucket.api :as db]
            [noob.bogus :as bogus :refer [bill propeller-hat stick]]
            [noob.slash.core :as slash]
            [noob.slash.inventory]
            [noob.spec-helper :as spec-helper]
            [noob.style.core :as style]
            [noob.user :as user]
            [speclj.core :refer :all]))

(declare request)

(describe "Inventory"
  (with-stubs)
  (spec-helper/stub-discord)
  (bogus/with-kinds :all)

  (with request (spec-helper/->slash-request "inventory" @bill
                  :member {:user {:avatar      "bill-avatar"
                                  :global-name "Bill"}}))

  (context "display"

    (it "has no items"
      (slash/handle-name @request)
      (spec-helper/should-have-replied-ephemeral @request "Your inventory is empty."))

    (it "has one item"
      (db/tx (user/loot @bill @stick))
      (slash/handle-name @request)
      (spec-helper/should-have-replied @request
        [:<> [:button {:id (:id @stick) :class "primary"} "Stick"]]
        :embed {:title       "Inventory"
                :description "Stick ⚔️ 1 ⭐️ 1"
                :color       style/green
                :author      (user/->author (:member @request))}))

    (it "has two items"
      (-> @bill
          (user/loot @stick)
          (user/loot @propeller-hat)
          db/tx)
      (slash/handle-name @request)
      (spec-helper/should-have-replied @request
        [:<>
         [:button {:id (:id @propeller-hat) :class "primary"} "Propeller Hat"]
         [:button {:id (:id @stick) :class "primary"} "Stick"]]
        :embed {:title       "Inventory"
                :description "Propeller Hat 👁 2 ⭐️ 2\nStick ⚔️ 1 ⭐️ 1"
                :color       style/green
                :author      (user/->author (:member @request))}))

    (it "has two items - one equipped"
      (-> @bill
          (user/loot @propeller-hat)
          (user/loot @stick)
          (user/equip @stick)
          db/tx)
      (slash/handle-name @request)
      (spec-helper/should-have-replied @request
        [:<>
         [:button {:id (:id @stick) :class "success"} "Stick"]
         [:button {:id (:id @propeller-hat) :class "primary"} "Propeller Hat"]]
        :embed {:title       "Inventory"
                :description "Stick ⚔️ 1 ⭐️ 1\nPropeller Hat 👁 2 ⭐️ 2"
                :color       style/green
                :author      (user/->author (:member @request))})))

  )
