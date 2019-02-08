(ns race-clock.db)

(def default-db
  {:clock-mode :automatic
   :clock-running false
   :clock-state :ready
   :clock-cound-down-start 5
   :clock-race-finish-count 5
   :clock-race-duration (* 2 60000)
   :clock-break-duration (* 3 60000)
   :clock-duration 0
   :clock-time-start 0
   :clock-time-elapsed 0
   :clock-time-remaining 0})
