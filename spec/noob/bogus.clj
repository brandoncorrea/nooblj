(ns noob.bogus
  (:require [c3kit.apron.corec :as ccc]
            [c3kit.apron.log :as log]
            [c3kit.bucket.api :as db]
            [c3kit.bucket.spec-helperc :as helperc]
            [noob.product :as product]
            [noob.schema.command :as command.schema]
            [noob.schema.product :as product.schema]
            [noob.schema.user :as user.schema]
            [noob.user :as user]
            [speclj.core :refer :all])
  (:import (clojure.lang IDeref)))

(def schemas [user.schema/all
              command.schema/all
              product.schema/all])

(deftype Entity [atm kind]
  ;; MDM - An Entity is reloaded from the database each time is de-referenced (@).
  ;; It's super convenient for test code.
  IDeref
  (deref [_]
    (if @atm
      (db/reload @atm)
      (log/warn "Using nil entity.  Maybe add (with-kinds " (or kind "<kind") ")"))))

(defn e-atom [entity] (.atm entity))
(defn entity [kind] (Entity. (atom nil) kind))

(defn init-entity! [entity & opt-args]
  (let [values (ccc/->options opt-args)]
    (reset! (e-atom entity) (db/tx values))))

(defmulti -init-kind! identity)

(def bill (entity :user))
(def ted (entity :user))
(defmethod -init-kind! :user [_]
  (init-entity! bill (db/tx (user/create "bill-id") :xp 100))
  (init-entity! ted (user/create! "ted-id")))

(def stick (entity :product))
(def propeller-hat (entity :product))
(defmethod -init-kind! :product [_]
  (init-entity! stick (product/create! "Stick" :main-hand 1 100 :description "A sticky stick." :attack 1))
  (init-entity! propeller-hat (product/create! "Propeller Hat" :head 2 250 :perception 2)))

(def deps
  ;; Add entities here with a list of entities they depend on (shallow).
  {:user    []
   :product []
   :all     [:user :product]})

(defmethod -init-kind! :all [_])

(def initialized-kinds (atom #{}))

(defn- maybe-init-kind! [kind]
  (when-not (contains? @initialized-kinds kind)
    (-init-kind! kind)
    (swap! initialized-kinds conj kind)))

(defn init! [& kinds]
  (assert (seq kinds))
  (loop [kinds kinds]
    (if-let [kind (first kinds)]
      (if-let [reqs (seq (remove @initialized-kinds (get deps kind)))]
        (recur (concat reqs kinds))
        (do
          (maybe-init-kind! kind)
          (recur (rest kinds))))
      @initialized-kinds)))

(defn with-kinds [& kinds]
  (list
    (helperc/with-schemas schemas)
    (before (reset! initialized-kinds #{})
            (apply init! kinds))
    ))
