(ns noob.core
  (:require [clojure.string :as str]))

(defn ->hash-map [key-fn value-fn coll]
  (apply hash-map (mapcat (juxt key-fn value-fn) coll)))

(defn ** [n pow]
  (cond
    (pos? pow) (apply * 1 (repeat pow n))
    (neg? pow) (apply / 1 (repeat (- pow) n))
    :else 1))

(defn niblet-term [amount]
  (str amount " Niblet" (when (not= 1 amount) "s")))

(defn join-lines [& lines] (str/join "\n" lines))

(defn some= [thing coll] (some #(= thing %) coll))