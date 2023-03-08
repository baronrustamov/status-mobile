(ns status-im2.contexts.communities.menus.channel-options.view
  (:require [utils.i18n :as i18n]
            [utils.re-frame :as rf]
            [quo2.core :as quo]))

(defn hide-sheet-and-dispatch
  [event]
  (rf/dispatch [:bottom-sheet/hide])
  (rf/dispatch event))

(defn mute-channel-action
  [chat-id]
  (hide-sheet-and-dispatch [:chat.ui/mute chat-id true]))

(defn unmute-channel-action
  [chat-id]
  (hide-sheet-and-dispatch [:chat.ui/mute chat-id false]))

(defn view-members
  [id]
  {:icon                :i/members
   :accessibility-label :view-members
   :label               (i18n/label :t/view-channel-members-and-details)})

(defn view-token-gating
  [id]
  {:icon                :i/token
   :right-icon          :i/chevron-right
   :chevron?            true
   :accessibility-label :view-token-gating
   :on-press            #(js/alert (str "implement action" id))
   :label               (i18n/label :t/view-token-gating)})

(defn mark-as-read
  [id]
  {:icon                :i/up-to-date
   :accessibility-label :mark-as-read
   :label               (i18n/label :t/mark-as-read)
   :on-press            #()})

(defn mute-channel
  [id muted?]
  {:icon                (if muted? :i/muted :i/activity-center)
   :accessibility-label (if muted? :unmute-channel :mute-channel)
   :label               (i18n/label (if muted? :t/unmute-channel :t/mute-channel))
   :on-press            (if muted?
                          #(unmute-channel-action id)
                          #(mute-channel-action id))
   :right-icon          :i/chevron-right
   :chevron?            true})

(defn fetch-messages
  []
  {:icon                :i/save
   :label               (i18n/label :t/fetch-messages)
   :on-press            #(js/alert "TODO: to be implemented, requires design input")
   :danger?             false
   :accessibility-label :fetch-messages
   :sub-label           nil
   :right-icon          :i/chevron-right
   :chevron?            true})

(defn invite-contacts
  [id]
  {:icon                :i/add-user
   :accessibility-label :invite-people-from-contacts
   :label               (i18n/label :t/invite-people-from-contacts)
   :on-press            #(hide-sheet-and-dispatch [:communities/invite-people-pressed id])})

(defn show-qr
  [id]
  {:icon                :i/qr-code
   :accessibility-label :show-qr
   :on-press            #(js/alert (str "implement action" id))
   :label               (i18n/label :t/show-qr)})

(defn share-channel
  [id]
  {:icon                :i/share
   :accessibility-label :share-channel
   :label               (i18n/label :t/share-channel)
   :on-press            #()})

(defn notifications
  []
  {:icon                :i/notifications
   :label               (i18n/label :t/notifications)
   :on-press            #(js/alert "TODO: to be implemented, requires design input")
   :danger?             false
   :sub-label           (i18n/label :t/all-messages)
   :accessibility-label :manage-notifications
   :right-icon          :i/chevron-right
   :chevron?            true
   :add-divider?        false})

(defn channel-options
  [community-id id token-gated? muted?]
  [[(view-members id)
    (when token-gated? (view-token-gating id))
    (mark-as-read id)
    (mute-channel (str community-id id) muted?)
    (notifications)
    (fetch-messages)
    (invite-contacts id)
    (show-qr id)
    (share-channel id)]])

(defn channel-options-bottom-sheet
  [community-id id]
  (let [{:keys [token-gated?]} (rf/sub [:communities/community id])
        {:keys [muted]}        (rf/sub [:chat-by-id (str community-id id)])]
    [quo/action-drawer
     (channel-options community-id id token-gated? muted)]))
