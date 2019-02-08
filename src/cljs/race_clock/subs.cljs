(ns race-clock.subs
  (:require
   [re-frame.core :as re-frame]
   [goog.string :as gstring]
   [goog.string.format]))

(defn format-mills
  [runtime]
  (let [minutes (/ runtime 60000)
        seconds (mod (/ runtime 1000) 60)]
    (str (gstring/format "%02d:%02d" minutes seconds))))

(re-frame/reg-sub
 ::name
 (fn [db]
   (:name db)))

(re-frame/reg-sub
 ::time-elapsed
 (fn [db]
   (format-mills
    (:clock-time-elapsed db))))

(re-frame/reg-sub
 ::time-remaining
 (fn [db]
   (format-mills
    (:clock-time-remaining db))))

(re-frame/reg-sub
 ::clock-state
 (fn [db]
   (:clock-state db)))

(re-frame/reg-sub
 ::clock-running
 (fn [db]
   (:clock-running db)))