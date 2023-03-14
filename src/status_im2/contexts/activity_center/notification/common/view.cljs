(ns status-im2.contexts.activity-center.notification.common.view
  (:require [quo2.core :as quo]
            [react-native.core :as rn]
            [react-native.gesture :as gesture]
            [quo2.foundations.colors :as colors]
            [status-im.multiaccounts.core :as multiaccounts]
            [status-im2.contexts.activity-center.notification.common.style :as style]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn user-avatar-tag
  [user-id]
  (let [contact (rf/sub [:contacts/contact-by-identity user-id])]
    [quo/user-avatar-tag
     {:color          :purple
      :override-theme :dark
      :size           :small
      :style          style/user-avatar-tag
      :text-style     style/user-avatar-tag-text}
     (:primary-name contact)
     (multiaccounts/displayed-photo contact)]))

(defn- render-swipe-action
  [{:keys [active-swipeable
           extra-fn
           interpolation-opacity
           interpolation-translate-x
           on-press
           swipe-button
           swipeable-ref]}]
  (fn [_ ^js drag-x]
    (let [{:keys [height] :as extra} (extra-fn)
          opacity                    (.interpolate drag-x interpolation-opacity)
          translate-x                (.interpolate drag-x interpolation-translate-x)]
      [gesture/rect-button
       {:style               {:border-radius style/swipe-button-border-radius}
        :accessibility-label :notification-swipe-action-button
        :on-press            (fn []
                               (when @swipeable-ref
                                 (.close ^js @swipeable-ref)
                                 (reset! active-swipeable nil))
                               (on-press extra))}
       [swipe-button
        {:style {:opacity   opacity
                 :transform [{:translateX translate-x}]
                 :height    height}}
        extra]])))

(defn- close-active-swipeable
  [active-swipeable swipeable]
  (fn [_]
    (when (and @active-swipeable
               (not= @active-swipeable @swipeable))
      (.close ^js @active-swipeable))
    (reset! active-swipeable @swipeable)))

(defn swipe-button-container
  [{:keys [style icon text]} _]
  [rn/animated-view
   {:accessibility-label :notification-swipe
    :style               style}
   [rn/view {:style style/swipe-text-wrapper}
    [quo/icon icon
     {:color colors/white}]
    [quo/text {:style style/swipe-text}
     text]]])

(defn swipe-button-read-or-unread
  [{:keys [style]} {:keys [notification]}]
  [swipe-button-container
   {:style (style/swipe-primary-container style)
    :icon  (if (:read notification)
             :i/notifications
             :i/check)
    :text  (if (:read notification)
             (i18n/label :t/unread)
             (i18n/label :t/read))}])

(defn swipe-button-delete
  [{:keys [style]}]
  [swipe-button-container
   {:style (style/swipe-danger-container style)
    :icon  :i/delete
    :text  (i18n/label :t/delete)}])

(defn swipe-on-press-toggle-read
  [{:keys [notification]}]
  (if (:read notification)
    (rf/dispatch [:activity-center.notifications/mark-as-unread (:id notification)])
    (rf/dispatch [:activity-center.notifications/mark-as-read (:id notification)])))

(defn swipe-on-press-delete
  [{:keys [notification]}]
  (rf/dispatch [:activity-center.notifications/delete (:id notification)]))

(defn swipeable
  [_]
  (let [swipeable-ref (atom nil)]
    (fn [{:keys [active-swipeable
                 extra-fn
                 left-button
                 left-on-press
                 right-button
                 right-on-press]}
         & children]
      (into
       [gesture/swipeable
        (merge
         {:ref                    #(reset! swipeable-ref %)
          :accessibility-label    :notification-swipeable
          :friction               2
          :on-swipeable-will-open (close-active-swipeable active-swipeable swipeable-ref)}
         (when left-button
           {:overshoot-left      false
            :left-threshold      style/swipe-action-width
            :render-left-actions (render-swipe-action
                                  {:active-swipeable active-swipeable
                                   :extra-fn extra-fn
                                   :interpolation-opacity style/left-swipe-opacity-interpolation-js
                                   :interpolation-translate-x
                                   style/left-swipe-translate-x-interpolation-js
                                   :on-press left-on-press
                                   :swipe-button left-button
                                   :swipeable-ref swipeable-ref})})
         (when right-button
           {:overshoot-right      false
            :right-threshold      style/swipe-action-width
            :render-right-actions (render-swipe-action
                                   {:active-swipeable active-swipeable
                                    :extra-fn extra-fn
                                    :interpolation-opacity style/right-swipe-opacity-interpolation-js
                                    :interpolation-translate-x
                                    style/right-swipe-translate-x-interpolation-js
                                    :on-press right-on-press
                                    :swipe-button right-button
                                    :swipeable-ref swipeable-ref})}))]
       children))))
