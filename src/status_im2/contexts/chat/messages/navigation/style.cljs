(ns status-im2.contexts.chat.messages.navigation.style
  (:require [quo2.foundations.colors :as colors]
            [react-native.reanimated :as reanimated]))

(def navigation-bar-height 100)

(defn button-container
  [position]
  (merge
   {:width            32
    :height           32
    :border-radius    10
    :justify-content  :center
    :align-items      :center
    :background-color (colors/theme-colors colors/white-opa-40 colors/neutral-80-opa-40)}
   position))

(defn blur-view
  [animation]
  (reanimated/apply-animations-to-style
   {:opacity animation}
   {:position       :absolute
    :top            0
    :left           0
    :right          0
    :height         navigation-bar-height
    :width          "100%"
    :display        :flex
    :flex-direction :row
    :overflow       :hidden}))

(def navigation-view
  {:z-index 4})

(def header-container
  {:position       :absolute
   :top            56
   :left           0
   :right          0
   :padding-bottom 12
   :width          "100%"
   :display        :flex
   :flex-direction :row
   :overflow       :hidden})

(defn header
  [y-animation opacity-animation]
  (reanimated/apply-animations-to-style
   ;; here using `left` won't work on Android, so we are using `translateX`
   {:transform [{:translateY y-animation}]
    :opacity   opacity-animation}
   {:flex 1}))

(defn pinned-banner
  [animation]
  (reanimated/apply-animations-to-style
   {:opacity animation}
   {:position :absolute
    :width    "100%"
    :top      navigation-bar-height}))

(def header-text-container
  {:flex-direction :row
   :align-items    :center
   :margin-left    8
   :margin-right   8})

(def header-display-name
  {:margin-left 8
   :flex        1})