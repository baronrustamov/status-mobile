(ns status-im2.contexts.chat.messages.navigation.view
  (:require [quo2.core :as quo]
            [quo2.components.avatars.user-avatar :as user-avatar]
            [quo2.foundations.colors :as colors]
            [re-frame.db]
            [react-native.core :as rn]
            [react-native.platform :as platform]
            [react-native.reanimated :as reanimated]
            [status-im2.contexts.chat.messages.navigation.style :as style]
            [status-im2.contexts.chat.messages.pin.banner.view :as pin.banner]
            [status-im2.constants :as constants]
            [utils.re-frame :as rf]
            [utils.i18n :as i18n]))

(defn navigation-view
  [{:keys [scroll-y]}]
  (let [{:keys [group-chat chat-id chat-name emoji
                chat-type]} (rf/sub [:chats/current-chat-chat-view])
        display-name        (if (= chat-type constants/one-to-one-chat-type)
                              (first (rf/sub [:contacts/contact-two-names-by-identity chat-id]))
                              (str emoji " " chat-name))
        online?             (rf/sub [:visibility-status-updates/online? chat-id])
        contact             (when-not group-chat (rf/sub [:contacts/contact-by-address chat-id]))
        photo-path          (when-not (empty? (:images contact)) (rf/sub [:chats/photo-path chat-id]))]
    (fn []
      [:f>
       (fn []
         (let [opacity-animation        (reanimated/interpolate scroll-y
                                                                [-100 0]
                                                                [0 1]
                                                                {:extrapolateLeft  "clamp"
                                                                 :extrapolateRight "extend"})
               banner-opacity-animation (reanimated/interpolate scroll-y
                                                                [80 120]
                                                                [0 1]
                                                                {:extrapolateLeft  "clamp"
                                                                 :extrapolateRight "clamp"})
               translate-animation      (reanimated/interpolate scroll-y
                                                                [0 50]
                                                                [50 0]
                                                                {:extrapolateLeft  "clamp"
                                                                 :extrapolateRight "clamp"})
               title-opacity-animation  (reanimated/interpolate scroll-y
                                                                [0 50]
                                                                [0 1]
                                                                {:extrapolateLeft  "clamp"
                                                                 :extrapolateRight "clamp"})]
           [rn/view {:style style/navigation-view}
            [reanimated/blur-view
             {:blurAmount   32
              :blurType     (colors/theme-colors :light :dark)
              :overlayColor (if platform/ios? colors/white :transparent)
              :style        (style/blur-view opacity-animation)}]

            [rn/view {:style {:display :flex}}
             [rn/view {:style style/header-container}
              [rn/touchable-opacity
               {:active-opacity 1
                :on-press       #(rf/dispatch [:navigate-back])
                :style          (style/button-container {:margin-left 20})}
               [quo/icon :i/arrow-left
                {:size 20 :color (colors/theme-colors colors/black colors/white)}]]
              [reanimated/view {:style (style/header translate-animation title-opacity-animation)}
               [rn/view {:style style/header-text-container}
                (when-not group-chat
                  [rn/view {:style {:margin-right 8}}
                   [user-avatar/user-avatar
                    {:full-name       display-name
                     :online?         online?
                     :profile-picture photo-path
                     :size            :small}]])
                [rn/view {:style {:flex 1}}
                 [quo/text
                  {:weight          :semi-bold
                   :size            :paragraph-1
                   :number-of-lines 1
                   :style           {:color (colors/theme-colors colors/black colors/white)}}
                  display-name]
                 (when online?
                   [quo/text
                    {:number-of-lines 1
                     :weight          :regular
                     :size            :paragraph-2
                     :style           {:color (colors/theme-colors colors/neutral-80-opa-50 colors/white-opa-50)}}
                    (i18n/label :t/online)])]]]
              [rn/touchable-opacity
               {:active-opacity 1
                :style          (style/button-container {:margin-right 20})}
               [quo/icon :i/options {:size 20 :color (colors/theme-colors colors/black colors/white)}]]]

             [reanimated/view {:style (style/pinned-banner banner-opacity-animation)}
              [pin.banner/banner chat-id]]]]))])))
