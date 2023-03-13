(ns status-im2.contexts.onboarding.enable-biometrics.view
  (:require [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [status-im2.contexts.onboarding.enable-biometrics.style :as style]
            [utils.i18n :as i18n]
            [status-im2.contexts.onboarding.common.background.view :as background]
            [utils.re-frame :as rf]
            [status-im.multiaccounts.biometric.core :as biometric]))

(defn navigation-bar
  []
  [rn/view {:style style/navigation-bar}
   [quo/page-nav
    {:align-mid?  true
     :mid-section {:type :text-only :main-text ""}
    }]])

(defn page
  []
  (let [supported-biometric (rf/sub [:supported-biometric-auth])
        bio-type-label (biometric/get-label supported-biometric)]
   [rn/view {:style style/page-container}
    [navigation-bar]
    [rn/view {:style {:padding-horizontal 20}}
     [quo/text
      {:size   :heading-1
       :weight :semi-bold
       :style  {:color colors/white}} (i18n/label :t/enable-biometrics)]
     [quo/text
      {:size   :paragraph-1
       :style  {:color      colors/white
                :margin-top 8}}
      (i18n/label :t/use-biometrics)]
     [rn/view {:style style/image-container}
      [quo/text {:size   :paragraph-1}
       "Illustration here"]]
     [rn/view 
      [quo/button
      {:on-press       #(rf/dispatch [:navigate-to :enable-notifications])
       :type           :grey
       :override-theme :dark
       :style          {}} (i18n/label :t/biometric-enable-button {:bio-type-label bio-type-label})]
     [quo/button
      {:on-press       #(rf/dispatch [:navigate-to :enable-notifications])
       :type           :grey
       :override-theme :dark
       :style          {:margin-top 12}} (i18n/label :t/maybe-later)]]]]))

(defn enable-biometrics
  []
  [rn/view {:style {:flex 1}}
   [background/view true]
   [page]])
