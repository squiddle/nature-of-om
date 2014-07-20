(ns noo.core
  (:require-macros [cljs.core.async.macros :refer [go-loop go]])
  (:require [noo.introduction]
            [noo.utils :as u]

            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [om.art :as art :include-macros true]

            [goog.dom :as gdom]
            [goog.events :as events]
            [cljs.core.async :as async :refer [>! <! put! alts! chan mult tap untap timeout close!]])

  (:import [goog.events EventType]))
(enable-console-print!)



(defn- add-chapter [global [component data]]
  (om/build component [data global]))

(defn noo [app]
  (reify
    om/IWillMount
    (will-mount [_]
      (let [mouse-chan
            (async/map
              (fn [e] [(.-clientX e) (.-clientY e)])
              [(u/listen js/window EventType/MOUSEMOVE)])]
        (go (while true
              (om/update! app [:global :mouse] (<! mouse-chan))))))
    om/IRender
    (render [this]
      (apply dom/div
             nil
             (map (partial add-chapter (:global app)) (:chapters app))))))


(om/root noo
         {:global       {:bounds [300 200]
                         :ticker (u/create-ticker (/ 1000 5))
                         :mouse nil}
          :chapters     [[noo.introduction/chapter {}]]}
         {:target (.getElementById js/document "noo")})
