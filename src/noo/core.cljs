(ns noo.core
  (:require-macros [cljs.core.async.macros :refer [go-loop go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [om.art :as art :include-macros true]

            [goog.events :as events]
            [cljs.core.async :as async :refer [>! <! put! alts! chan mult tap untap timeout close!]])

  (:import [goog.events EventType]))

(def processing (js/Processing.))


(defn listen [el type]
  (let [out (chan)]
    (events/listen el type #(put! out %))
    out))

(def rand (.-random processing))
(def noise (.-noise processing))
(defn pos->svg-path-pair [pos]
  (str (:x pos) " " (:y pos)))

(defn line-to-pos [pos]
  (str "L " (pos->svg-path-pair pos)))
(defn move-to-pos [pos]
  (str "M " (pos->svg-path-pair pos)))

(defn trail [positions owner]
  (reify
    om/IRender
    (render [this]
      (art/Shape #js{:stroke "#888888" :d (apply str (move-to-pos (last positions)) (map line-to-pos (rest (take 100 (reverse positions)))))}))))


(defn rand-walk [position]
  (assoc position
    :x (+ (:x position) (rand -1 1))
    :y (+ (:y position) (rand -1 1))))

(defn noise-walk [position]
  (let [x (:x position)
        y (:y position)]
    (.log js/console "noise walk" (noise x))
    (assoc position
      :x (+ x (if (< (noise x) 0.5) 1 -1))
      :y (+ y (if (< (noise y) 0.5) 1 -1)))))

(defn mouse-walk [position mouse-pos]
  (assoc position
    :x (+ (:x position) (if (> (:x mouse-pos) (:x position)) 1 -1))
    :y (+ (:y position) (if (> (:y mouse-pos) (:y position)) 1 -1))))

(defn walker [[positions ticker mouse-pos] owner]
  (reify
    om/IWillMount
    (will-mount [this]
      (let [ticker-channel (tap ticker (chan))]
        (go (while true
              (<! ticker-channel)
              (om/transact! positions (fn [positions]
                                        (conj positions (let [position (last positions)]
                                                          (if (and (not (nil? @mouse-pos)) (< (rand) 0)) (mouse-walk position @mouse-pos) (rand-walk position))))))))))
    om/IRender
    (render [this]
      (let [position (last positions)]
        (art/Group nil
                   (om/build trail positions)
                   (art/Shape #js{:stroke "#ff0000" :fill "#00ff00" :d (str "M " (:x position) " " (:y position) " m -5 0 a 5,5 0 1,0 10,0 a 5,5 0 1,0 -10,0")}))))))

(defn widget [app]
  (reify
    om/IWillMount
    (will-mount [_]
      (let [mouse-chan
            (async/map
              (fn [e] {:x (.-clientX e) :y (.-clientY e)})
              [(listen js/window EventType/MOUSEMOVE)])]
        (go (while true
              (om/update! app :mouse (<! mouse-chan)))))
      )
    om/IRender
    (render [_]
      (art/Surface #js{:width 640 :height 480}
                   (art/Group nil
                              (om/build walker [(:positions app) (:ticker app) (:mouse app)]))))))

(defn create-ticker [delay]
  (let [ticker (chan)
        ticker-mult (mult ticker)]
    (go-loop []
             (<! (timeout delay))
             (>! ticker :test)
             (recur))
    ticker-mult))

(om/root widget {:mouse {:x 123 :y 123} :bounds [640 480] :positions [{:x 50 :y 50}] :ticker (create-ticker (/ 1000 15))}
         {:target (.getElementById js/document "app")})
