(ns status-im2.contexts.add-new-contact.views
  (:require
    [clojure.string :as string]
    [quo2.core :as quo]
    [react-native.core :as rn]
    [react-native.clipboard :as clipboard]
    [status-im2.common.resources :as resources]
    [status-im.qr-scanner.core :as qr-scanner]
    [status-im.utils.utils :as utils]
    [status-im2.contexts.add-new-contact.style :as style]
    [utils.debounce :as debounce]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn found-contact
  [public-key]
  (let [{:keys [primary-name
                compressed-key
                identicon
                images]} (rf/sub [:contacts/contact-by-identity public-key])
        profile-picture  (-> (or (:thumbnail images) (:large images) (first images))
                             (get :uri identicon))]
    (when primary-name
      [rn/view style/found-user
       [quo/text (style/text-description)
        (i18n/label :t/user-found)]
       [rn/view (style/found-user-container)
        [quo/user-avatar
         {:full-name         primary-name
          :profile-picture   profile-picture
          :size              :small
          :status-indicator? false}]
        [rn/view style/found-user-text
         [quo/text
          {:weight :semi-bold
           :size   :paragraph-1
           :style  (style/found-user-display-name)}
          primary-name]
         [quo/text
          {:weight :regular
           :size   :paragraph-2
           :style  (style/found-user-key)}
          (utils/get-shortened-address compressed-key)]]]])))

(defn new-contact
  []
  (let [{:keys [input public-key state error ens-name]} (rf/sub [:contacts/new-identity])
        error?                                          (and (= state :error)
                                                             (= error :uncompressed-key))]
    [rn/keyboard-avoiding-view (style/container-kbd)
     [rn/view style/container-image
      [rn/image
       {:source (resources/get-image :add-new-contact)
        :style  style/image}]
      [quo/button
       (merge (style/button-close)
              {:on-press
               (fn []
                 (rf/dispatch [:contacts/clear-new-identity])
                 (rf/dispatch [:navigate-back]))}) :i/close]]
     [rn/view (style/container-outer)
      [rn/view style/container-inner
       [quo/text (style/text-title)
        (i18n/label :t/add-a-contact)]
       [quo/text (style/text-subtitle)
        (i18n/label :t/find-your-friends)]
       [quo/text (style/text-description)
        (i18n/label :t/ens-or-chat-key)]
       [rn/view style/container-text-input
        [rn/view (style/text-input-container error?)
         [rn/text-input
          (merge (style/text-input)
                 {:default-value  input
                  :placeholder    (i18n/label :t/type-some-chat-key)
                  :on-change-text #(debounce/debounce-and-dispatch
                                    [:contacts/set-new-identity %]
                                    600)})]
         (when (string/blank? input)
           [quo/button
            (merge style/button-paste
                   {:on-press (fn []
                                (clipboard/get-string #(rf/dispatch [:contacts/set-new-identity %])))})
            (i18n/label :t/paste)])]
        [quo/button
         (merge style/button-qr
                {:on-press #(rf/dispatch [::qr-scanner/scan-code
                                          {:handler :contacts/qr-code-scanned}])})
         :i/scan]]
       (when error?
         [rn/view style/container-error
          [quo/icon :i/alert style/icon-error]
          [quo/text style/text-error (i18n/label :t/not-a-chatkey)]])
       (when (= state :valid)
         [found-contact public-key])]
      [rn/view
       [quo/button
        (merge (style/button-view-profile state)
               {:on-press
                (fn []
                  (rf/dispatch [:contacts/clear-new-identity])
                  (rf/dispatch [:navigate-back])
                  (rf/dispatch [:chat.ui/show-profile public-key ens-name]))})
        (i18n/label :t/view-profile)]]]]))
