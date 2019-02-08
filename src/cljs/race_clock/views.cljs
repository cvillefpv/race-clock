(ns race-clock.views
  (:require
   [re-frame.core :as re-frame]
   [race-clock.subs :as subs]
   [race-clock.events :as evt]))

(defn break-clock 
  [tr]
  [:div 
   [:div "Next Race Starts In"]
   [:div {:style {:font-size "2em"}} @tr]])

(defn race-clock 
  [tr te]
  [:div 
   [:div "Time Elapsed"]
   [:div {:style {:font-size "2em"}} @te]
   [:div {:style {:padding-top "20px"}} "Time Left"]
   [:div {:style {:font-size "2em"}} @tr]])

(defn main-panel []
  (let [tr (re-frame/subscribe [::subs/time-remaining])
        te (re-frame/subscribe [::subs/time-elapsed])
        st (re-frame/subscribe [::subs/clock-state])
        cr (re-frame/subscribe [::subs/clock-running])]
    [:div {:style {:text-align "center" 
                   :padding-top "100px"}}
     [:div {:style {:height "150px"}}
     (when (or (= @st :ready) (= @st :racing))
       (race-clock tr te))
     (when (= @st :break)
       (break-clock tr))]
     [:div {:style {:margin-top "80px"}}
      (if @cr
        [:button {:style {:font-size "1.75em"}
                  :on-click #(re-frame/dispatch [::evt/stop-clock])}
         "Stop"]
        [:button {:style {:font-size "1.75em"}
                  :on-click #(re-frame/dispatch [::evt/start-count-down])}
         "Start"])]]))
