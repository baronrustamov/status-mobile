(ns status-im2.contexts.chat.messages.list.style
  (:require [quo2.foundations.colors :as colors]
            [react-native.reanimated :as reanimated]))

(def cover-height 168)
(def cover-bg-color "#2A799B33")

(def footer
  {:z-index 5})

(defn header-bottom-part
  [animation]
  (reanimated/apply-animations-to-style
   {:border-top-right-radius animation
    :border-top-left-radius  animation}
   {:top              -16
    :background-color (colors/theme-colors colors/white colors/neutral-100)
    :shadow-radius    16
    :shadow-opacity   1
    :shadow-color     "rgba(9, 16, 28, 0.06)"
    :shadow-offset    {:width 0 :height -24}}))

(def header-avatar
  {:margin-top    -36
   :margin-left   20
   :margin-right  20
   :margin-bottom 0})