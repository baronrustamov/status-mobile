(ns status-im2.contexts.onboarding.sign-in.sign-in-by-syncing.style
  (:require [quo2.foundations.colors :as colors]))

(def screen-padding 20)

(def absolute-fill
  {:position :absolute
   :top      0
   :bottom   0
   :left     0
   :right    0})

(def header-container
  {:flex-direction     :row
   :justify-content    :space-between
   :padding-horizontal screen-padding
   :margin-vertical    12})

(def header-text
  {:padding-horizontal screen-padding
   :padding-top        12
   :padding-bottom     8
   :color              colors/white})

(def header-sub-text
  {:padding-horizontal screen-padding
   :color              colors/white})

(def tabs-container
  {:padding-horizontal screen-padding
   :margin-top         20})

(def camera-permission-container
  {:height            335
   :margin-horizontal screen-padding
   :background-color  colors/white-opa-5
   :border-color      colors/white-opa-10
   :border-radius     12
   :margin-top        20
   :align-items       :center
   :justify-content   :center})

(def enable-camera-access-header
  {:color colors/white})

(def enable-camera-access-sub-text
  {:color         colors/white-opa-70
   :margin-bottom 16})

(def enter-sync-code-container
  {:margin-top      20
   :justify-content :center
   :align-items     :center})

(defn bottom-container
  [insets]
  {:padding-top             12
   :padding-bottom          (:bottom insets)
   :background-color        colors/white-opa-5
   :border-top-left-radius  20
   :border-top-right-radius 20
   :align-items             :center
   :justify-content         :center})

(def bottom-text
  {:color          colors/white
   :padding-bottom 12})