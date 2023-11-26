(ns noob.slash.core-spec
  (:require [c3kit.apron.corec :as ccc]
            [discord.option :as option]
            [noob.slash.core :as sut]
            [speclj.core :refer :all]))

(defmacro should-have-command
  ([commands name description] `(should-have-command ~commands ~name ~description nil))
  ([commands name description options]
   `(let [[_# -description# -options#] (ccc/ffilter #(= ~name (first %)) ~commands)]
      (should= ~description -description#)
      (should= ~options -options#))))

(defmacro dev-should-have
  ([name description] `(should-have-command sut/dev-commands ~name ~description))
  ([name description options] `(should-have-command sut/dev-commands ~name ~description ~options)))

(defmacro global-should-have
  ([name description] `(should-have-command sut/global-commands ~name ~description))
  ([name description options] `(should-have-command sut/global-commands ~name ~description ~options)))

(describe "Slash Core"

  (it "dev commands"
    (should= 0 (count sut/dev-commands)))

  (it "global commands"
    (should= 9 (count sut/global-commands))
    (global-should-have "attack" "Attack another player!"
                        [(option/->user! "target" "The person you want to attack.")])
    (global-should-have "daily" "Redeem your daily Niblets!")
    (global-should-have "give" "Give some niblets to that special someone"
                        [(option/->user! "recipient" "The recipient of your handout")
                         (option/->int! "amount" "The number of niblets to bestow")])
    (global-should-have "inventory" "See your inventory!")
    (global-should-have "love" "Love another player ❤️"
                        [(option/->user! "beloved" "That special someone 🫶")])
    (global-should-have "shop" "Get in, loser. We're going shopping!")
    (global-should-have "stats" "View your player stats.")
    (global-should-have "steal" "Steal Niblets from another player!"
                        [(option/->user! "victim" "The person you will be stealing from.")])
    (global-should-have "weekly" "Redeem your weekly Niblets!"))

  (it "normalizes options"
    (should-be-nil (sut/normalize-options nil))
    (should= {} (sut/normalize-options {}))
    (should= {:a :b} (sut/normalize-options {:a :b}))
    (should= {:a :b :data {:options nil :c :d}} (sut/normalize-options {:a :b :data {:options nil :c :d}}))
    (should= {:a :b :data {:options {} :c :d}} (sut/normalize-options {:a :b :data {:options [] :c :d}}))
    (should= {:data {:options {"hello" 1 "world" "blah"}}}
             (sut/normalize-options {:data {:options [{:name "hello" :value 1 :foo :bar}
                                                      {:name "world" :value "blah" :bar :baz}]}})))
  )
