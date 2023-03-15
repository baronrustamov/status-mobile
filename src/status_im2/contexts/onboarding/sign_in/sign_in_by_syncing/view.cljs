(ns status-im2.contexts.onboarding.sign-in.sign-in-by-syncing.view
  (:require ["react-native-camera-kit" :refer (CameraKitCamera)]
            [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.blur :as blur]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.common.resources :as resources]
            [status-im2.contexts.onboarding.sign-in.sign-in-by-syncing.style :as style]
            [utils.re-frame :as rf]
            [react-native.safe-area :as safe-area]
            [taoensso.timbre :as log]
            [utils.i18n :as i18n]
            [react-native.hole-view :as hole-view]
            [react-native.permissions :as permissions]))

(defonce active-tab (reagent/atom 1))
(defonce camera-permission-granted (reagent/atom false))

(def camera (reagent/adapt-react-class CameraKitCamera))

(defn- header
  []
  [rn/view
   [rn/view {:style style/header-container}
    [quo/button
     {:icon                true
      :type                :blur-bg
      :size                32
      :accessibility-label :close-sign-in-by-syncing
      :override-theme      :dark
      :on-press            #(rf/dispatch [:navigate-back])}
     :i/close]
    [quo/button
     {:before              :i/info
      :type                :blur-bg
      :size                32
      :accessibility-label :find-sync-code
      :override-theme      :dark
      :on-press            #(js/alert "Yet to be implemented")}
     (i18n/label :t/find-sync-code)]]
   [quo/text
    {:size   :heading-1
     :weight :semi-bold
     :style  style/header-text}
    (i18n/label :t/sign-in-by-syncing)]
   [quo/text
    {:size   :paragraph-1
     :weight :regular
     :style  style/header-sub-text}
    (i18n/label :t/synchronise-your-data-across-your-devices)]
   [rn/view {:style style/tabs-container}
    [quo/segmented-control
     {:size           32
      :override-theme :dark
      :blur?          true
      :default-active @active-tab
      :data           [{:id 1 :label (i18n/label :t/scan-sync-qr-code)}
                       {:id 2 :label (i18n/label :t/enter-sync-code)}]
      :on-change      #(reset! active-tab %)}]]])



(defn- enable-camera-access-view
  []
  (let [on-request-camera-permission (fn []
                                       (rf/dispatch
                                        [:request-permissions
                                         {:permissions [:camera]
                                          :on-allowed
                                          #(do
                                             ;;  TODO ! CLEANUP THIS
                                             (log/info " CAMERA PERMISSION REQUEST SUCCESS!")
                                             (reset! camera-permission-granted true))
                                          :on-denied
                                          #(js/setTimeout
                                            (fn []
                                              (log/error " CAMERA PERMISSION REQUEST ERROR"))
                                            50)}]))]

    [rn/view {:style style/camera-permission-container}
     [quo/text
      {:size   :paragraph-1
       :weight :medium
       :style  style/enable-camera-access-header}
      (i18n/label :t/enable-access-to-camera)]
     [quo/text
      {:size   :paragraph-2
       :weight :regular
       :style  style/enable-camera-access-sub-text}
      (i18n/label :t/to-scan-a-qr-enable-your-camera)]
     [quo/button
      {:before              :i/camera
       :type                :primary
       :size                32
       :accessibility-label :request-camera-permission
       :override-theme      :dark
       :on-press            on-request-camera-permission}
      (i18n/label :t/enable-camera)]]))

(defn- scan-qr-code-tab
  []
  [enable-camera-access-view])

(defn- enter-sync-code-tab
  []
  [rn/view {:style style/enter-sync-code-container}
   [quo/text
    {:size   :paragraph-1
     :weight :medium
     :style  {:color colors/white}}
    "Yet to be implemented"]])

(defn- bottom-view
  [insets]
  [rn/touchable-without-feedback
   {:on-press #(js/alert "Yet to be implemented")}
   [rn/view
    {:style (style/bottom-container insets)}
    [quo/text
     {:size   :paragraph-2
      :weight :regular
      :style  style/bottom-text}
     (i18n/label :t/i-dont-have-status-on-another-device)]]])


(defn view
  []
  [:f>
   (fn []
     (let [insets     (safe-area/use-safe-area)
           camera-ref (reagent/atom nil)]
       (permissions/permission-granted? :camera
                                        #(reset! camera-permission-granted %)
                                        #(reset! camera-permission-granted false))
       [rn/view {:style {:flex 1 :padding-top (:top insets)}}
        [camera
         {:ref            #(reset! camera-ref %)
          :style          (merge style/absolute-fill {:flex 1 :background-color :white})
          :camera-options {:zoomMode :off}
          :scan-barcode   true
          :on-read-code   #(do
                             (log/info %))}]
        [rn/image
         {:style  {:position :absolute :top 0 :left 0 :right 0 :bottom 0 :height "100%" :width "100%"}
          :source (resources/get-image :intro-4)}]
        [hole-view/hole-view
         {:style style/absolute-fill
          ;;   :holes [{:x 20 :y 253 :width 345 :height 335 :border-radius 12}]
         }
         [blur/view
          {:style         style/absolute-fill
           :overlay-color colors/neutral-80-opa-80
           :blur-amount   20}]]

        [header]
        (case @active-tab
          1 [scan-qr-code-tab]
          2 [enter-sync-code-tab]
          nil)
        [rn/view {:style {:flex 1}}]
        [bottom-view insets]]))])