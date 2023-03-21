(ns status-im2.contexts.onboarding.syncing.syncing-devices.view
  (:require [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [status-im2.contexts.onboarding.syncing.syncing-devices.style :as style]
            [utils.i18n :as i18n]
            [status-im2.contexts.onboarding.common.background.view :as background]
            [utils.re-frame :as rf]
            [reagent.core :as reagent]))

(defn page-title
  [syncing-on-progress? syncing-completed?]
  [rn/view {:style {:flex 1}}
   [quo/text
    {:accessibility-label :notifications-screen-title
     :weight              :semi-bold
     :size                :heading-1
     :style               {:color colors/white}}
    (i18n/label (if syncing-on-progress?
                  :t/synced-devices-progress-title
                  (if syncing-completed?
                    :t/synced-devices-title
                    :t/synced-devices-error-title)))]
   [quo/text
    {:accessibility-label :notifications-screen-sub-title
     :weight              :regular
     :size                :paragraph-1
     :style               {:color colors/white}}
    (i18n/label (if syncing-on-progress?
                  :t/synced-devices-progress-sub-title
                  (if syncing-completed?
                    :t/synced-devices-sub-title
                    :t/synced-devices-error-sub-title)))]])

(defn navigation-bar
  []
  [rn/view {:style style/navigation-bar}
   [quo/page-nav
    {:align-mid?            true
     :mid-section           {:type :text-only :main-text ""}
     :left-section          {:type                :blur-bg
                             :icon                :i/arrow-left
                             :icon-override-theme :dark
                             :on-press            #(rf/dispatch [:navigate-back])}
     :right-section-buttons [{:type                :blur-bg
                              :icon                :i/info
                              :icon-override-theme :dark
                              :on-press            #(js/alert "Pending")}]}]])


(defn render-device-item
  []
  [rn/view {:style {:flex             1
                    :height           100
                    :border-radius    16
                    :border-width     1
                    :border-color     colors/white-opa-5
                    :background-color colors/white-opa-5}}
   [quo/text
    {:accessibility-label :notifications-screen-title
     :weight              :semi-bold
     :size                :heading-1
     :style               {:color colors/white}}
    (i18n/label :t/synced-devices-error-title)]
   [quo/text
    {:accessibility-label :notifications-screen-sub-title
     :weight              :regular
     :size                :paragraph-1
     :style               {:color colors/white}}
    (i18n/label :t/synced-devices-error-sub-title)]
   [quo/status-tag
    {:size   :small
     :label  "This device"
     :blur?  false}]])

(defn device-list
  [devices]
  [rn/view {:style  {:flex  1}}
   [rn/flat-list
    {:key-fn                            :id
     :keyboard-should-persist-taps      :always
     :separator                         [rn/view {:width 12}]
     :data                              devices
     :render-fn                         render-device-item}]])

(defn page
  [syncing-on-progress? syncing-completed? devices]
  [rn/view {:style style/page-container}
   [navigation-bar]
   [rn/view {:style {:padding-horizontal 20}}
    [background/view true]
    [navigation-bar]
    [page-title]
    (when syncing-on-progress?
      [rn/view {:style style/page-illustration}
       [quo/text
        "[Illustration here]"]]
      [rn/view {:style style/page-illustration}
       [quo/text
        "[Illustration here]"]
       [quo/button
        {:on-press                  #(rf/dispatch [:init-root :enable-notifications])
         :accessibility-label       :enable-notifications-later-button
         :override-background-color (colors/custom-color :blue 60)
         :style                     {:margin-top 20}}
        (i18n/label :t/try-again)]])
    (when syncing-completed?
      [rn/view {:style  {:flex 1}}
       [device-list devices]
       [quo/button
        {:on-press                  #(rf/dispatch [:init-root :enable-notifications])
         :accessibility-label       :enable-notifications-later-button
         :override-background-color (colors/custom-color :blue 60)
         :style                     {:margin-top 20}}
        (i18n/label :t/continue)]])]])

(defn syncing-devices
  []
  (let [syncing-completed? (reagent/atom nil)
        syncing-on-progress? (reagent/atom nil)
        devices  (reagent/atom nil)]
    [rn/view {:style {:flex 1}}
     [background/view true]
     [page syncing-on-progress? syncing-completed? devices]]))
