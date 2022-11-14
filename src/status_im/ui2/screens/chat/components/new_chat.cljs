(ns status-im.ui2.screens.chat.components.new-chat
  (:require [cljs.spec.alpha :as spec]
            [clojure.string :as string]
            [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [status-im.constants :as constants]
            [status-im.i18n.i18n :as i18n]
            [status-im.ui.components.chat-icon.screen :as chat-icon]
            [status-im.multiaccounts.core :as multiaccounts]
            [status-im.ui.components.keyboard-avoid-presentation
             :as
             kb-presentation]
            [status-im.ui.components.invite.views :as invite]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.ui2.components.search-input :as search]
            [status-im.ui.components.toolbar :as toolbar]
            [status-im.ui.components.topbar :as topbar]
            [status-im.ui.screens.group.styles :as styles]
            [quo.core :as quo]
            [quo2.core :as quo2]
            [quo2.components.buttons.button :as button]
            [status-im.utils.handlers :refer [<sub]]
            [status-im.utils.debounce :as debounce]
            [quo2.foundations.colors :as quo2.colors])
  (:require-macros [status-im.utils.views :as views]))

(defn- render-contact [row]
  (let [[first-name second-name] (multiaccounts/contact-two-names row false)]
    [quo2/list-item
     {:title    first-name
      :subtitle second-name
      :icon     [chat-icon/contact-icon-contacts-tab
                 (multiaccounts/displayed-photo row)]}]))

(defn- on-toggle [allow-new-users? checked? public-key]
  (cond

    checked?
    (re-frame/dispatch [:deselect-contact public-key allow-new-users?])

    ;; Only allow new users if not reached the maximum
    (and (not checked?)
         allow-new-users?)
    (re-frame/dispatch [:select-contact public-key allow-new-users?])))

(defn- on-toggle-participant [allow-new-users? checked? public-key]
  (cond

    checked?
    (re-frame/dispatch [:deselect-participant public-key allow-new-users?])

   ;; Only allow new users if not reached the maximum
    (and (not checked?)
         allow-new-users?)
    (re-frame/dispatch [:select-participant public-key allow-new-users?])))

(defn- toggle-item []
  (fn [allow-new-users? subs-name {:keys [public-key] :as contact} on-toggle]
    (let [contact-selected? @(re-frame/subscribe [subs-name public-key])
          [first-name second-name] (multiaccounts/contact-two-names contact true)]
      [quo2/list-item
       {:title            first-name
        :text-color       (quo2.colors/theme-colors quo2.colors/neutral-100 quo2.colors/white)
        :subtitle         second-name
        :icon             [chat-icon/contact-icon-contacts-tab
                           (multiaccounts/displayed-photo contact)]
        :on-press         #(on-toggle allow-new-users? contact-selected? public-key)
        :active           contact-selected?
        :accessory        :checkbox
        :background-color (quo2.colors/theme-colors quo2.colors/white quo2.colors/neutral-90)}])))

(defn- group-toggle-contact [{:keys [:allow-new-users?] :as contact} _ _]
  [toggle-item allow-new-users? :is-contact-selected? contact on-toggle])

(defn- group-toggle-participant [{:keys [:allow-new-users?] :as contact} _ _]
  [toggle-item allow-new-users? :is-participant-selected? contact on-toggle-participant])

(defn toggle-list [{:keys [contacts render-fn]}]
  [list/section-list
   {:content-container-style        {:padding-vertical 8}
    :key-fn                         :id
    :keyboard-should-persist-taps   :always
    :sticky-section-headers-enabled false
    :sections                       contacts
    :render-section-header-fn       quo2/index
    :render-fn                      render-fn}])

(defn no-contacts [{:keys [no-contacts]}]
  [react/view {:style styles/no-contacts}
   [react/text
    {:style (styles/no-contact-text)}
    no-contacts]
   [invite/button]])

(defn filter-contacts [filter-text contacts]
  (let [lower-filter-text (string/lower-case (str filter-text))
        filter-fn         (fn [{:keys [data]}]
                            (let [{:keys [name alias nickname]} (first data)]
                              (or
                               (string/includes? (string/lower-case (str name)) lower-filter-text)
                               (string/includes? (string/lower-case (str alias)) lower-filter-text)
                               (when nickname
                                 (string/includes? (string/lower-case (str nickname)) lower-filter-text)))))]
    (if filter-text
      (filter filter-fn contacts)
      contacts)))

;; Set name of new group-chat
(views/defview new-group []
  (views/letsubs [contacts   [:selected-group-contacts]
                  group-name [:new-chat-name]]
                 (let [group-name-empty? (not (spec/valid? :global/not-empty-string group-name))]
                   [react/keyboard-avoiding-view  {:style styles/group-container
                                                   :ignore-offset true}
                    [react/view {:flex 1}
                     [topbar/topbar {:use-insets false
                                     :title      (i18n/label :t/new-group-chat)
                                     :subtitle   (i18n/label :t/group-chat-members-count
                                                             {:selected (inc (count contacts))
                                                              :max      constants/max-group-chat-participants})}]
                     [react/view {:style {:padding-top 16
                                          :flex        1}}
                      [react/view {:style {:padding-horizontal 16}}
                       [quo/text-input
                        {:auto-focus          true
                         :on-change-text      #(re-frame/dispatch [:set :new-chat-name %])
                         :default-value       group-name
                         :placeholder         (i18n/label :t/set-a-topic)
                         :accessibility-label :chat-name-input}]
                       [react/text {:style (styles/members-title)}
                        (i18n/label :t/members-title)]]
                      [react/view {:style {:margin-top 8
                                           :flex       1}}
                       [list/flat-list {:data                         contacts
                                        :key-fn                       :address
                                        :render-fn                    render-contact
                                        :bounces                      false
                                        :keyboard-should-persist-taps :always
                                        :enable-empty-sections        true}]]]
                     [toolbar/toolbar
                      {:show-border? true
                       :left
                       [quo/button {:type                :secondary
                                    :before              :main-icon/back
                                    :accessibility-label :previous-button
                                    :on-press            #(re-frame/dispatch [:navigate-back])}
                        (i18n/label :t/back)]
                       :right
                       [quo/button {:type                :secondary
                                    :accessibility-label :create-group-chat-button
                                    :disabled            group-name-empty?
                                    :on-press            #(debounce/dispatch-and-chill [:group-chats.ui/create-pressed group-name]
                                                                                       300)}
                        (i18n/label :t/create-group-chat)]}]]])))

(defn searchable-contact-list []
  (let [search-value (reagent/atom nil)]
    (fn [{:keys [contacts no-contacts-label toggle-fn allow-new-users? show-cancel?]}]
      [react/view {:style {:flex 1}}
       [react/view {:style (styles/search-container)}
        [search/search-input {:on-cancel            #(reset! search-value nil)
                              :search-border-radius 0
                              :show-cancel          show-cancel?
                              :search-border-width  0
                              :on-change            #(reset! search-value %)}]]
       [react/view {:style {:flex             1
                            :padding-vertical 8
                            :background-color (quo2.colors/theme-colors quo2.colors/white quo2.colors/neutral-90)}}
        (if (seq contacts)
          [toggle-list {:contacts    (filter-contacts @search-value contacts)
                        :render-data allow-new-users?
                        :render-fn   toggle-fn}]
          [no-contacts {:no-contacts no-contacts-label}])]])))

;; Start group chat
(defn contact-toggle-list []
  (let [contacts                   (<sub [:contacts/sorted-and-grouped-by-first-letter])
        selected-contacts-count    (<sub [:selected-contacts-count])
        one-contact-selected?      (= selected-contacts-count 1)
        no-contacts-selected?      (zero? selected-contacts-count)
        {:keys [alias public-key]} (-> contacts first :data first)]
    [react/keyboard-avoiding-view {:style         styles/group-container
                                   :ignore-offset true}
     [topbar/topbar {:use-insets    false
                     :border-bottom false
                     :modal?        true
                     :background    (quo2.colors/theme-colors quo2.colors/white quo2.colors/neutral-90)}]
     [react/view {:style {:flex-direction     :row
                          :justify-content    :space-between
                          :align-items        :flex-end
                          :padding-horizontal 20}}
      [quo2/text {:weight :semi-bold
                  :size   :heading-1
                  :color  (quo2.colors/theme-colors quo2.colors/neutral-40 quo2.colors/neutral-50)}
       (i18n/label :t/new-chat)]
      [quo2/text {:size            :paragraph-2
                  :weight          :regular
                  :secondary-color (quo2.colors/theme-colors quo2.colors/neutral-40 quo2.colors/neutral-50)}
       (i18n/label :t/selected-count-from-max
                   {:selected (inc selected-contacts-count)
                    :max      constants/max-group-chat-participants})]]
     [searchable-contact-list
      {:contacts          contacts
       :show-cancel?      false
       :no-contacts-label (i18n/label :t/group-chat-no-contacts)
       :toggle-fn         group-toggle-contact}]
     (when-not no-contacts-selected?
       [toolbar/toolbar
        {:show-border?  false
         :margin-bottom 20
         :center        [button/button {:type                :primary
                                        :accessibility-label :next-button
                                        :on-press            #(if one-contact-selected?
                                                                (re-frame/dispatch [:chat.ui/start-chat public-key])
                                                                (re-frame/dispatch [:navigate-to :new-group]))}
                         (if one-contact-selected?
                           (i18n/label :t/chat-with {:selected-user alias})
                           (i18n/label :t/setup-group-chat))]}])]))

;; Add participants to existing group chat
(defn add-participants-toggle-list []
  (let [contacts                   (<sub [:contacts/all-contacts-not-in-current-chat])
        current-chat               (<sub [:chats/current-chat])
        selected-contacts-count    (<sub [:selected-participants-count])
        current-participants-count (count (:contacts current-chat))]
    [kb-presentation/keyboard-avoiding-view  {:style styles/group-container}
     [topbar/topbar {:use-insets    false
                     :border-bottom false
                     :title         (i18n/label :t/add-members)
                     :subtitle      (i18n/label :t/group-chat-members-count
                                                {:selected (+ current-participants-count selected-contacts-count)
                                                 :max      constants/max-group-chat-participants})}]
     [searchable-contact-list
      {:contacts          contacts
       :no-contacts-label (i18n/label :t/group-chat-all-contacts-invited)
       :toggle-fn         group-toggle-participant
       :allow-new-users?  (< (+ current-participants-count
                                selected-contacts-count)
                             constants/max-group-chat-participants)}]
     [toolbar/toolbar
      {:show-border? true
       :center       [quo/button {:type                :secondary
                                  :accessibility-label :next-button
                                  :disabled            (zero? selected-contacts-count)
                                  :on-press            #(re-frame/dispatch [:group-chats.ui/add-members-pressed])}
                      (i18n/label :t/add)]}]]))

(views/defview edit-group-chat-name []
  (views/letsubs [{:keys [name chat-id]} [:chats/current-chat]
                  new-group-chat-name (reagent/atom nil)]
                 [kb-presentation/keyboard-avoiding-view  {:style styles/group-container}
                  [react/scroll-view {:style {:padding 16
                                              :flex    1}}
                   [quo/text-input
                    {:on-change-text      #(reset! new-group-chat-name %)
                     :default-value       name
                     :on-submit-editing   #(when (seq @new-group-chat-name)
                                             (re-frame/dispatch [:group-chats.ui/name-changed chat-id @new-group-chat-name]))
                     :placeholder         (i18n/label :t/enter-contact-code)
                     :accessibility-label :new-chat-name
                     :return-key-type     :go}]]
                  [react/view {:style {:flex 1}}]
                  [toolbar/toolbar
                   {:show-border? true
                    :center
                    [quo/button {:type                :secondary
                                 :accessibility-label :done
                                 :disabled            (and (<= (count @new-group-chat-name) 1)
                                                           (not (nil? @new-group-chat-name)))
                                 :on-press            #(cond
                                                         (< 1 (count @new-group-chat-name))
                                                         (re-frame/dispatch [:group-chats.ui/name-changed chat-id @new-group-chat-name])

                                                         (nil? @new-group-chat-name)
                                                         (re-frame/dispatch [:navigate-back]))}
                     (i18n/label :t/done)]}]]))