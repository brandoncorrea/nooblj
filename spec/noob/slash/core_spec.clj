(ns noob.slash.core-spec
  (:require [c3kit.apron.corec :as ccc]
            [discord.option :as option]
            [noob.slash.core :as sut]
            [speclj.core :refer :all]))

(defn should-have-command
  ([commands name description] (should-have-command commands name description nil))
  ([commands name description options]
   (let [[_ -description -options] (ccc/ffilter #(= name (first %)) commands)]
     (should= description -description)
     (should= options -options))))

(def dev-should-have (partial should-have-command sut/dev-commands))

(describe "Slash Core"

  (it "dev commands"
    (dev-should-have "daily" "Redeem your daily Niblets!")
    (dev-should-have "weekly" "Redeem your weekly Niblets!")
    (dev-should-have "give"
                     "Give some niblets to that special someone"
                     [(option/->user! "recipient" "That special someone <3")
                      (option/->int! "amount" "The number of niblets to bestow")])
    (should= 3 (count sut/dev-commands)))

  (it "global commands"
    (should= 0 (count sut/global-commands)))

  (it "normalizes options"
    (should= nil (sut/normalize-options nil))
    (should= {} (sut/normalize-options {}))
    (should= {:a :b} (sut/normalize-options {:a :b}))
    (should= {:a :b :data {:options nil :c :d}} (sut/normalize-options {:a :b :data {:options nil :c :d}}))
    (should= {:a :b :data {:options {} :c :d}} (sut/normalize-options {:a :b :data {:options [] :c :d}}))
    (should= {:data {:options {"hello" 1 "world" "blah"}}}
             (sut/normalize-options {:data {:options [{:name "hello" :value 1 :foo :bar}
                                                      {:name "world" :value "blah" :bar :baz}]}})))
  )