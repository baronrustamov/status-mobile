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
            [utils.re-frame :as rf]))

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
                                                                [50 100]
                                                                [0 1]
                                                                {:extrapolateLeft  "clamp"
                                                                 :extrapolateRight "extend"})
               banner-opacity-animation (reanimated/interpolate scroll-y
                                                                [180 220]
                                                                [0 1]
                                                                {:extrapolateLeft  "clamp"
                                                                 :extrapolateRight "clamp"})
               translate-animation      (reanimated/interpolate scroll-y
                                                                [100 150]
                                                                [50 0]
                                                                {:extrapolateLeft  "clamp"
                                                                 :extrapolateRight "clamp"})
               title-opacity-animation  (reanimated/interpolate scroll-y
                                                                [100 150]
                                                                [0 1]
                                                                {:extrapolateLeft  "clamp"
                                                                 :extrapolateRight "clamp"})]
           [rn/view {:style style/navigation-view}
            [reanimated/blur-view
             {:blurAmount   32
              :blurType     :light
              :overlayColor (if platform/ios? colors/white-opa-70 :transparent)
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
                [quo/text
                 {:weight          :semi-bold
                  :number-of-lines 1
                  :style           style/header-display-name}
                 display-name]]]
              [rn/touchable-opacity
               {:active-opacity 1
                :style          (style/button-container {:margin-right 20})}
               [quo/icon :i/options {:size 20 :color (colors/theme-colors colors/black colors/white)}]]]

             [reanimated/view {:style (style/pinned-banner banner-opacity-animation)}
              [pin.banner/banner chat-id]]]]))])))
