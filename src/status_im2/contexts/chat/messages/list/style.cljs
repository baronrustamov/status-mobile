(ns status-im2.contexts.chat.messages.list.style
  (:require [quo2.foundations.colors :as colors]
            [react-native.reanimated :as reanimated]))

(def cover-height 168)
(def overscroll-cover-height 2000)
(def cover-bg-color "#2A799B33")

(def footer
  {:z-index 5})

(defn keyboard-avoiding-container
  [view-height]
  {:position :absolute
   :display  :flex
   :flex     1
   :top      0
   :left     0
   :height   view-height
   :right    0})

(def list-container
  {:padding-top    16
   :padding-bottom 16})

(defn header-bottom-part
  [animation]
  (reanimated/apply-animations-to-style
   {:border-top-right-radius animation
    :border-top-left-radius  animation}
   {:position         :relative
    :top              -16
    :margin-bottom    -16
    :padding-bottom   24
    :background-color (colors/theme-colors colors/white colors/neutral-100)
    :shadow-radius    16
    :shadow-opacity   1
    :shadow-color     "rgba(9, 16, 28, 0.06)"
    :shadow-offset    {:width 0 :height -24}
    :display          :flex}))

(def header-avatar
  {:top -36
   :margin-left   20
   :margin-right  20
   :margin-bottom -36})