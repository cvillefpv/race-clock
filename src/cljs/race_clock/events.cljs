(ns race-clock.events
  (:require
   [re-frame.core :as re-frame]
   [race-clock.db :as db]))

(defn announcement
  [msg]
  (let [m (js/SpeechSynthesisUtterance. msg)]
    (.speechSynthesis.speak js/window m)))

(defn gen-tone
  [freq duration]
  (let [ctx (js/window.AudioContext.)
        ocs (.createOscillator ctx)]
    (set! (.-type ocs) "sine")
    (set! (.-frequency.value ocs) freq)
    (.connect ocs ctx.destination)
    (.start ocs)
    (.stop ocs (+ ctx.currentTime duration))))

(def live-intervals (atom {}))

(defonce interval-handler
  (fn [{:keys [action id freq event]}]
    (condp = action
      :start (swap! live-intervals
                    assoc id
                    (js/setInterval
                     #(re-frame/dispatch event)
                     freq))
      :end (js/clearInterval (get @live-intervals id)))))

(re-frame/reg-fx
 :interval
 interval-handler)

(re-frame/reg-event-db
 ::initialize-db
 (fn [_ c]
   (merge
    db/default-db
    (second c))))

(re-frame/reg-event-fx
 ::tick
 (fn [{:keys [db]} _]
   (let [clock-duration-mills (-> db :clock-duration)
         duration-mills (- (js/Date.now)
                           (-> db :clock-time-start))
         clock-remaining-mills (- clock-duration-mills duration-mills)
         clock-remaining (if (pos? clock-remaining-mills)
                           clock-remaining-mills
                           0)]
     {:db
      (-> db
          (assoc :clock-time-remaining clock-remaining)
          (assoc :clock-time-elapsed (+ duration-mills 1000)))})))

(re-frame/reg-event-fx
 ::make-announcement
 (fn [_ m]
   (announcement (second m))))

(re-frame/reg-event-fx
 ::start-race-complete-count-down
 (fn [_ _]
   {:interval {:action :start
               :id :race-end-count-down
               :event [::race-count-down]
               :freq 1000}}))

(re-frame/reg-event-fx
 ::race-count-down
 (fn [{:keys [db]} _]
   (let [c (-> db :clock-race-finish-count)]
     (if (<= c 0)
       (do
         (gen-tone 880 1)
         {:interval {:action :end
                     :id :race-end-count-down}
          :dispatch [::race-completed]})
       (do (announcement (str c))
           {:db (-> db (assoc :clock-race-finish-count (dec c)))})))))

(re-frame/reg-event-fx
 ::count-down
 (fn [{:keys [db]} _]
   (let [c (-> db :clock-cound-down-start)
         res {:db (-> db (assoc :clock-cound-down-start (dec c)))}]
     (case c 
       5 res
       4 res
       3 (do 
           (gen-tone 440 0.5)
           res)
       2 (do (gen-tone 440 0.5)
             res)
       1 (do (gen-tone 440 0.5)
             res)
       0 (do
           (gen-tone 880 1)
           (re-frame/dispatch [::clock-start])
           res)
       (do
         (assoc res :interval {:id :count-down
                               :action :end}))))))

(re-frame/reg-event-fx
 ::start-count-down
 (fn [{:keys [db]} _]
   (announcement "Racers ready")
   {:db (-> db (assoc :clock-running true))
    :interval {:action :start
               :id :count-down
               :event [::count-down]
               :freq 1000}}))

(re-frame/reg-event-fx
 ::clock-start
 (fn [{:keys [db]} _]
   (let [d (-> db :clock-race-duration)]
     {:interval {:action :start
                 :id :clock
                 :event [::tick]
                 :freq 200}
      :dispatch-later [{:ms (- d (* 31 1000)) 
                        :dispatch [::make-announcement "30 seconds remaining"]}
                       {:ms (- d (* 7 1000))
                        :dispatch [::start-race-complete-count-down]}]
      :db (-> db
              (assoc :clock-duration d)
              (assoc :clock-time-start (js/Date.now))
              (assoc :clock-state :racing))})))

(re-frame/reg-event-fx
 ::race-completed
 (fn [_ _]
   (announcement "Race is over")
   {:interval {:action :end
               :id :clock}
    :dispatch [::initialize-db {:clock-running true}]
    :dispatch-later [{:ms (* 10 1000)
                     :dispatch [::start-break]}]}))

(re-frame/reg-event-fx
 ::stop-clock 
 (fn [_ _]
   (doseq [i (vals @live-intervals)]
     (js/clearInterval i))
   (doseq [t (range 0 (js/setTimeout nil 0))]
     (js/clearTimeout t))
   {:dispatch [::initialize-db]}))

(re-frame/reg-event-fx
 ::break-completed
 (fn [_ _]
   (announcement "Next race is about to begin")
   {:dispatch [::initialize-db {:clock-running true}]
    :interval {:action :end
               :id :break-clock}
    :dispatch-later [{:ms (* 10 1000)
                      :dispatch [::start-count-down]}]}))

(re-frame/reg-event-fx
 ::start-break
 (fn [{:keys [db]} _]
   (let [d (-> db :clock-break-duration)
         m (/ d 60000)]
     (announcement (str "Next race will start in " m " minutes."))
     {:db (-> db 
              (assoc :clock-time-start (js/Date.now))
              (assoc :clock-duration d)
              (assoc :clock-state :break))
      :interval {:action :start
                 :id :break-clock
                 :event [::tick]
                 :freq 100}
      :dispatch-later [{:ms (- d 60000)
                        :dispatch [::make-announcement "One minute till next race."]}
                       {:ms (- d 30000)
                        :dispatch [::make-announcement "Thirty seconds till next race."]}
                       {:ms d
                        :dispatch [::break-completed]}]})))