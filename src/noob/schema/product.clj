(ns noob.schema.product
  (:require [c3kit.apron.schema :as s]
            [noob.core :as core]))

(def slots #{:main-hand :off-hand :head :torso :legs :hands :feet :back})

(def product
  {:kind        (s/kind :product)
   :id          core/auto-int-id-type
   :slot        {:type :keyword}
   :name        {:type :string}
   :description {:type :string}
   :price       {:type :long}
   :level       {:type :long}
   :attack      {:type :long}
   :defense     {:type :long}
   :sneak       {:type :long}
   :perception  {:type :long}
   })

(def all [product])
