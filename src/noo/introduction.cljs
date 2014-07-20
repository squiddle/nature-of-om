(ns noo.introduction
  (:require-macros [cljs.core.async.macros :refer [go-loop go]])
  (:require [om.core :as om :include-macros true]
            [om.art :as art :include-macros true]
            [cljs.core.async :as async :refer [>! <! put! alts! chan mult tap untap timeout close!]]
            [noo.utils :as u]))


(defn trail [positions owner]
  "renders a light gray line of the given positions"
  (reify
    om/IRender
    (render [this]
      (art/Shape #js{:stroke "#aaaaaa" :d (apply str (u/line->svg-path (reverse positions)))}))))





(defn rand-walk [position]
  [(+ (first position) (js/Math.round (u/random -1 1)))
   (+ (second position) (js/Math.round (u/random -1 1)))])

(defn noise-walk [position]
  (let [x (first position)
        y (second position)]
    (println "noise walk" (u/noise x))
    [(+ x (if (< (u/noise x) 0.5) 1 -1))
     (+ y (if (< (u/noise y) 0.5) 1 -1))]))

(defn mouse-walk [position mouse-pos]
  (let [x (first position)
        y (second position)]
    [(+ x (if (> (first mouse-pos) x) 1 -1))
     (+ y (if (> (second mouse-pos) y) 1 -1))]))

(defn walk [positions mouse-pos bounds]
  (let [position (if (empty? positions)
                   [(js/Math.round (u/random 0 (first bounds))) (js/Math.round (u/random 0 (second bounds)))]
                   (last positions))]
    (if (and mouse-pos (< (u/random) 0.5))
      (mouse-walk position mouse-pos)
      (rand-walk position))))


(defn walker [[{:keys [positions] :as data} {:keys [ticker bounds] :as global}] owner]
  "a simple walker who will walk on each tick"
  (reify
    om/IWillMount
    (will-mount [this]
      (let [ticker-channel (tap ticker (chan))]
        (go (while true
              (<! ticker-channel)
              (om/transact! data
                            [:positions]
                            (fn [positions]
                              (let [mouse (:mouse @global)
                                    pos (if (nil? positions) [] positions)]
                                (conj pos (walk pos mouse @bounds)))))))))
    om/IRender
    (render [this]
      (if-let [position (last positions)]
        (art/Group nil
                   (om/build trail positions)
                   (art/Shape #js{:stroke "#222222" :fill "#666666" :d (u/circle->svg-path position 3)}))
        (art/Group nil)))))

(defn chapter [[data global] owner]
  (reify
    om/IRender
    (render [this]
      (art/Surface #js{:width (first (:bounds global)) :height (second (:bounds global))}
                   (art/Group nil
                              (om/build walker [data global]))))))
