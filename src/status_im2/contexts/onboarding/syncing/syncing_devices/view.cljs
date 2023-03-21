(ns status-im2.contexts.onboarding.syncing.syncing-devices.view
  (:require [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [status-im2.contexts.onboarding.syncing.syncing-devices.style :as style]
            [utils.i18n :as i18n]
            [status-im2.contexts.onboarding.common.background.view :as background]
            [utils.re-frame :as rf]))


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

(defn page
  []
  [rn/view {:style style/page-container}
   [navigation-bar]
   [rn/view {:style {:padding-horizontal 20}}
    [quo/text
     {:size   :heading-1
      :weight :semi-bold
      :style  {:color colors/white}} "Syncing devices..."]
    [quo/text
     {:size   :heading-2
      :weight :semi-bold
      :style  {:color colors/white}} "will show sync complete if successful"]
    [quo/text
     {:size   :heading-2
      :weight :semi-bold
      :style  {:color colors/white}} "will show sync failed if unsuccessful"]
    [quo/button
     {:on-press #(rf/dispatch [:navigate-to :enable-notifications])
      :style    {}} (i18n/label :t/continue)]]])

(defn syncing-devices
  []
  [rn/view {:style {:flex 1}}
   [background/view true]
   [page]])
