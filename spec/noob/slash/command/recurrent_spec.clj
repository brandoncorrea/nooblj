(ns noob.slash.command.recurrent-spec
  (:require [c3kit.apron.time :as time]
            [c3kit.bucket.api :as db]
            [noob.bogus :as bogus :refer [bill]]
            [noob.command :as command]
            [noob.slash.core :as slash]
            [noob.slash.command.recurrent]
            [noob.spec-helper :as spec-helper :refer [should-have-replied]]
            [speclj.core :refer :all]))

(defn ->recurrent-request [name {:keys [discord-id]} username]
  {:data   {:name name}
   :member {:user {:id discord-id :username username}}})
(def ->daily-request (partial ->recurrent-request "daily"))
(def ->weekly-request (partial ->recurrent-request "weekly"))

(def now (time/now))
(def bill-request)

(describe "Recurrent Command"
  (with-stubs)
  (spec-helper/stub-discord)
  (spec-helper/stub-now now)
  (bogus/with-kinds :user)

  (context "daily"
    (with bill-request (->daily-request @bill "Bill"))

    (it "one hour until next execution"
      (command/create-daily-command! @bill (-> now (time/before (time/days 1)) (time/after (time/hours 1))))
      (slash/handle-command @bill-request)
      (should-have-replied @bill-request "Your daily reward will be ready in 1 hour!"))

    (it "three hours twelve minutes until next execution"
      (command/create-daily-command! @bill (-> now (time/before (time/days 1)) (time/after (time/hours 3)) (time/after (time/minutes 12))))
      (slash/handle-command @bill-request)
      (should-have-replied @bill-request "Your daily reward will be ready in 3 hours and 12 minutes!"))

    (it "succeeds with missing user"
      (let [request (->daily-request {:discord-id "napoleon-id"} "Napoleon")]
        (slash/handle-command request)
        (let [{:keys [id niblets]} (db/ffind-by :user :discord-id "napoleon-id")]
          (should= 20 niblets)
          (should-have-replied request "You received 20 Niblets!")
          (should= now (:last-ran-at (db/ffind-by :command :user id :interval :daily))))))

    (it "succeeds with missing command"
      (slash/handle-command @bill-request)
      (should= 20 (:niblets @bill))
      (should-have-replied @bill-request "You received 20 Niblets!")
      (should= now (:last-ran-at (db/ffind-by :command :user (:id @bill) :interval :daily))))

    (it "succeeds with existing command"
      (command/create-daily-command! @bill (-> 5 time/days time/ago))
      (slash/handle-command @bill-request)
      (should= 20 (:niblets @bill))
      (should-have-replied @bill-request "You received 20 Niblets!")
      (should= 1 (db/count-by :command :user (:id @bill) :interval :daily))
      (should= now (:last-ran-at (db/ffind-by :command :user (:id @bill) :interval :daily))))

    (it "succeeds when user has niblets"
      (db/tx @bill :niblets 100)
      (slash/handle-command @bill-request)
      (should= 120 (:niblets @bill))
      (should-have-replied @bill-request "You received 20 Niblets!")
      (should= 1 (db/count-by :command :user (:id @bill) :interval :daily))
      (should= now (:last-ran-at (db/ffind-by :command :user (:id @bill) :interval :daily))))

    (it "20% bonus when less than 48 hours from last reward"
      (command/create-daily-command! @bill (time/ago (time/hours 47)))
      (slash/handle-command @bill-request)
      (should= 24 (:niblets @bill))
      (should-have-replied @bill-request "You received 24 Niblets!")
      (should= 1 (db/count-by :command :user (:id @bill) :interval :daily))
      (should= now (:last-ran-at (db/ffind-by :command :user (:id @bill) :interval :daily))))
    )

  (context "weekly"
    (with bill-request (->weekly-request @bill "Bill"))

    (it "one day until next execution"
      (command/create-weekly-command! @bill (time/before now (time/days 6)))
      (slash/handle-command @bill-request)
      (should-have-replied @bill-request "Your weekly reward will be ready in 1 day!"))

    (it "3 days 12 hours and 4 minutes until next execution"
      (command/create-weekly-command! @bill
                                      (-> now
                                          (time/before (time/days 7))
                                          (time/after (time/days 3))
                                          (time/after (time/hours 12))
                                          (time/after (time/minutes 4))))
      (slash/handle-command @bill-request)
      (should-have-replied @bill-request "Your weekly reward will be ready in 3 days, 12 hours, and 4 minutes!"))

    (it "succeeds with missing user"
      (let [request (->weekly-request {:discord-id "napoleon-id"} "Napoleon")]
        (slash/handle-command request)
        (let [{:keys [id niblets]} (db/ffind-by :user :discord-id "napoleon-id")]
          (should= 150 niblets)
          (should-have-replied request "You received 150 Niblets!")
          (should= now (:last-ran-at (db/ffind-by :command :user id :interval :weekly))))))

    (it "succeeds with missing command"
      (slash/handle-command @bill-request)
      (should= 150 (:niblets @bill))
      (should-have-replied @bill-request "You received 150 Niblets!")
      (should= now (:last-ran-at (db/ffind-by :command :user (:id @bill) :interval :weekly))))

    (it "succeeds with existing command"
      (command/create-weekly-command! @bill (time/ago (time/days 15)))
      (slash/handle-command @bill-request)
      (should= 150 (:niblets @bill))
      (should-have-replied @bill-request "You received 150 Niblets!")
      (should= 1 (db/count-by :command :user (:id @bill) :interval :weekly))
      (should= now (:last-ran-at (db/ffind-by :command :user (:id @bill) :interval :weekly))))

    (it "succeeds when user has niblets"
      (db/tx @bill :niblets 500)
      (slash/handle-command @bill-request)
      (should= 650 (:niblets @bill))
      (should-have-replied @bill-request (str "You received 150 Niblets!"))
      (should= 1 (db/count-by :command :user (:id @bill) :interval :weekly))
      (should= now (:last-ran-at (db/ffind-by :command :user (:id @bill) :interval :weekly))))

    (it "20% bonus when less than 14 days from last reward"
      (command/create-weekly-command! @bill (time/ago (time/days 13)))
      (slash/handle-command @bill-request)
      (should= 180 (:niblets @bill))
      (should-have-replied @bill-request "You received 180 Niblets!"))
    )
  )
