(ns status-im2.contexts.share.view
  (:require [utils.i18n :as i18n]
            [quo.react :as react]
            [quo2.core :as quo]
            [react-native.core :as rn]
            [react-native.safe-area :as safe-area]
            [status-im2.contexts.share.style :as style]
            [utils.re-frame :as rf]
            [taoensso.timbre :as log]
            [reagent.core :as reagent]
            [quo2.foundations.colors :as colors]
            ;;TODO(siddarthkay) : move these components over to status-im2 ns first
            [status-im.ui.components.qr-code-viewer.views :as qr-code-viewer]
            [status-im.ui.components.copyable-text :as copyable-text]
            [react-native.fast-image :as fast-image]
            [clojure.string :as string]))

(def ^:const profile-tab-id 0)
(def ^:const wallet-tab-id 1)

(defn header
  []
  [rn/view
   [quo/button
    {:icon                true
     :type                :blur-bg
     :size                32
     :accessibility-label :close-activity-center
     :override-theme      :dark
     :style               style/header-button
     :on-press            #(rf/dispatch [:hide-popover])}
    :i/close]
   [quo/text
    {:size   :heading-1
     :weight :semi-bold
     :style  style/header-heading}
    (i18n/label :t/share)]])

;;;;            emoji-hash       "🙈🤭🤓😂🤷🏻😈😇🤑🥳😍🥺"

(defn profile-tab [window-width]
      (let [multiaccounts          (rf/sub [:multiaccount/accounts])
            multiaccount           (rf/sub [:multiaccount])
            emoji-hash             (string/join " " (get multiaccount :emoji-hash))
            keyuid                 (get multiaccount :key-uid)
            current-pk             (rf/sub [:multiaccount/public-key]) ;; idk why this section breaks!
            port                   (rf/sub [:mediaserver/port])
            profile-qr-url         (str "https://join.status.im/u/"  current-pk)
            media-server-url       (str "https://localhost:"
                                    port
                                    "/GenerateQRCode?level=2&url="
                                    (js/btoa profile-qr-url)
                                    "&keyUid="
                                    keyuid
                                    "&allowProfileImage=true"
                                    "&size=200"
                                    "&imageName=large")
;            public-key     (multiaccount :public-key)
            ;;emoji-hash     (:emoji-hash multiaccount) ;;; TODO(siddarthay) : when multiaccount is created
                                                      ;;; make call back to native module statusgo and fetch
                                                      ;;; the emoji hash from and then store it in app-db

            ]
;        (log/info "media-server-url" media-server-url)
        (log/info "emoji-hash ->"  (string/join " " (get multiaccount :emoji-hash)))
      [:<>
       [rn/view {:style (style/qr-code-container window-width)}
        ;;; todo now qr-code-viewer can be replaced with either a fast-image or a
        ;;; react-native image component and we could directly call the media server
;        [qr-code-viewer/qr-code-view (* window-width 0.808) profile-qr-url 12 colors/white]
        [rn/view {:style {:flex-direction :row
                          :justify-content :center}}
;         [fast-image/fast-image
;          {:style  {:width  "100%"
;                    :height 300}
;           :source {:uri media-server-url}}]
            [rn/image {:source {:uri           media-server-url}
                       :style  {:width         303
                                :height        303
                                :margin-top    30
                                :border-radius 4
                                :margin-right  4}}
             ]
         ]


        [rn/view {:style style/profile-address-container}
         [rn/view {:style style/profile-address-column}
          [quo/text
           {:size :paragraph-2
            :weight :medium
            :style style/profile-address-label}
            (i18n/label :t/link-to-profile)
           ]
          [copyable-text/copyable-text-view {:copied-text profile-qr-url
                                             :container-style style/copyable-text-container-style}
           [rn/text {:style (style/profile-address-content (* window-width 0.7))
                     :ellipsize-mode :middle
                     :number-of-lines 1}
            profile-qr-url]]]

         [rn/view
          [quo/button
           {:icon                true
            :type                :blur-bg
            :size                32
            :accessibility-label :link-to-profile
            :override-theme      :dark
            :style               style/header-button
            ;;:on-press           ;;;; TODO(siddarthay) : figure this out and take appropriate action
            }
           :i/share]
          ]
         ]]

       [rn/view {:style style/emoji-hash-container}
         [rn/view {:style style/profile-address-container}
          [rn/view {:style style/profile-address-column}
           [quo/text {:size :paragraph-2
                      :weight :medium
                      :style style/emoji-hash-label}
            (i18n/label :t/emoji-hash)]
           [copyable-text/copyable-text-view {:copied-text emoji-hash :container-style style/copyable-text-container-style}
            [rn/view {:style (style/set-custom-width (* window-width 0.87))}
             [rn/text {:style (style/emoji-hash-content (* window-width 0.72))} emoji-hash]
             [rn/view
              [quo/button
               {:icon                true
                :type                :blur-bg
                :size                32
                :accessibility-label :link-to-profile
                :override-theme      :dark
                :style               style/header-button
                ;;:on-press           ;;;; TODO(siddarthay) : probably do nothing here? idk
                }
               :i/copy]
              ]
             ]]]]]
       ]))



(defn wallet-tab [window-width]
  [:<>
  [rn/text "wallet-tab"]
   ]
  )

(defn view []
  (let [selected-tab (reagent/atom profile-tab-id)]
     [safe-area/consumer
      (fn [{:keys [top bottom]}]
        (let [window-width  (rf/sub [:dimensions/window-width])]
          [rn/view {:style (style/screen-container window-width top bottom)}
           [header]
           [quo/tabs
            {:size                28
             :scrollable?         true
             :blur?               true
             :override-theme      :light
             :style               style/tabs
             :fade-end-percentage 0.79
             :scroll-on-press?    true
             :fade-end?           true
             :on-change        #(reset! selected-tab %)
             :default-active      @selected-tab
             :data                [{:id    profile-tab-id
                                    :label (i18n/label :t/profile)}
                                   {:id    wallet-tab-id
                                    :label (i18n/label :t/wallet)}]}]
            (if (= @selected-tab profile-tab-id)
                  [profile-tab window-width]
                  [wallet-tab window-width])]
          ))]))
