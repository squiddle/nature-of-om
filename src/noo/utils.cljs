(ns noo.utils
  (:require-macros [cljs.core.async.macros :refer [go-loop go]])
  (:require [goog.events :as events]
            [cljs.core.async :as async :refer [<! >! chan mult timeout]]))


(def processing "global Processing.js instance" (js/Processing.))
(def random "random function from Processing" (.-random processing))
(def noise "noise from Processing" (.-noise processing))




(defn listen [el type]
  "listen to the event of type on el and give back a channel"
  (let [out (async/chan)]
    (events/listen el type #(async/put! out %))
    out))


(defn create-ticker [delay]
  "returns a mult channel which will tick after every delay"
  (let [ticker (chan)
        ticker-mult (mult ticker)]
    (go (loop [tick 1]
          (>! ticker tick)
          (<! (timeout delay))
          (recur (inc tick))))
    ticker-mult))


;;
;; SVG Path helpers
;;

(defn pos->svg-path [pos]
  "takes positions"
  (str (first pos) " " (second pos)))

(defn svg-line-to [positions]
  (apply str "L" (interpose " " (map pos->svg-path positions))))
(defn svg-move-to [pos]
  (str "M" (pos->svg-path pos)))
(defn svg-move [pos]
  (str "m" (pos->svg-path pos)))
(defn line->svg-path [positions]
  (str (svg-move-to (first positions)) (svg-line-to (rest positions))))


(defn circle->svg-path [pos radius]
  (str (svg-move-to pos)
       (svg-move [(* -1 radius) 0])                         ;; move a bit back
       (str "a" radius " " radius " 0 1 0 " (* 2 radius) " 0") ;; first half arc to 2radius 0
       (str "a" radius " " radius " 0 1 0 " (* -2 radius) " 0"))) ;; second half arc back to -2radius 0