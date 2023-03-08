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

(def header
  {:position       :absolute
   :top            56
   :left           0
   :right          0
   :padding-bottom 8
   :width          "100%"
   :display        :flex
   :flex-direction :row
   :overflow       :hidden})

(defn animated-header
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

(def header-container
  {:flex-direction :row
   :align-items    :center
   :margin-left    8
   :margin-right   8
   :margin-top     -4
   :height         40})

(def header-avatar-container
  {:margin-right 8})

(def header-text-container
  {:flex 1})

(defn header-display-name []
  {:color (colors/theme-colors colors/black colors/white)})

(defn header-online []
  {:color (colors/theme-colors colors/neutral-80-opa-50 colors/white-opa-50)})