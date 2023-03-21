(ns status-im.native-module.mention
  (:require [status-im.utils.types :as types]
            [taoensso.timbre :as log]
            [status-im.native-module.core :refer [status]]
            [clojure.set :as set]))

(defn- transfer-input-segments
  [segments]
  (map (fn [segment]
         (let [type  (case (get segment "Type")
                       0 :text
                       1 :mention)
               value (get segment "Value")]
           [type value]))
       segments))

(defn- rename-at-idxs
  [at-idxs]
  (map #(set/rename-keys %
                         {:From      :from
                          :To        :to
                          :Checked   :checked?
                          :Mentioned :mention?
                          :Mention   :mention
                          :NextAtIdx :next-at-idx})
       at-idxs))

(defn- rename-state
  [state]
  (-> state
      (set/rename-keys {:AtSignIdx    :at-sign-idx
                        :AtIdxs       :at-idxs
                        :MentionEnd   :mention-end
                        :PreviousText :previous-text
                        :NewText      :new-text
                        :Start        :start
                        :End          :end})
      (update :at-idxs rename-at-idxs)))

; referenced function: status-im2.common.contact-list-item.view/contact-list-item
(defn- rename-mentionable-users
  [mentionable-users]
  (reduce (fn [acc [id val]]
            (assoc acc
                   id
                   (set/rename-keys val
                                    {:id            :public-key
                                     :primaryName   :primary-name
                                     :secondaryName :secondary-name
                                     :compressedKey :compressed-key
                                     :ensVerified   :ens-verified
                                     :added         :added?
                                     :displayName   :display-name
                                     :searchedText  :searched-text
                                    })))
          {}
          mentionable-users))

(defn- convert-to-clj
  [result need-convert-to-clj?]
  (if need-convert-to-clj?
    (types/json->clj result)
    result))

(defn- transfer-mention-result
  [result need-convert-to-clj?]
  (let [{:keys [input-segments mentionable-users state chat-id]}
        (-> result
            (convert-to-clj need-convert-to-clj?)
            (set/rename-keys {:InputSegments      :input-segments
                              :MentionSuggestions :mentionable-users
                              :MentionState       :state
                              :ChatID             :chat-id}))]
    {:chat-id           chat-id
     :input-segments    (transfer-input-segments input-segments)
     :mentionable-users (rename-mentionable-users mentionable-users)
     :state             (rename-state state)}))

(defn to-input-field
  [chat-id text]
  (log/debug "[native-module] to-input-field" {:chat-id chat-id :text text})
  (let [result                               (.mentionToInputField ^js (status) chat-id text)
        {:keys [newText chatMentionContext]} (convert-to-clj result true)
        chat-mention-context                 (transfer-mention-result chatMentionContext false)]
    (merge chat-mention-context {:new-text newText})))

(defn check-mentions
  [chat-id text]
  (log/debug "[native-module] check-mentions" {:chat-id chat-id :text text})
  (.mentionCheckMentions ^js (status) chat-id text))

(defn check-selection
  [chat-id text start end]
  (log/debug "[native-module] check-selection" {:chat-id chat-id :text text :start start :end end})
  (let [result (.mentionCheckSelection ^js (status) chat-id text start end)]
    (transfer-mention-result result true)))

(defn on-text-input
  [chat-id state]
  (log/debug "[native-module] on-text-input" {:chat-id chat-id :state state})
  (let [state             (set/rename-keys state
                                           {:previous-text :PreviousText
                                            :new-text      :NewText
                                            :start         :Start
                                            :end           :End})
        state-json-string (types/clj->json state)
        result            (.mentionOnTextInput ^js (status) chat-id state-json-string)]
    (transfer-mention-result result true)))

(defn recheck-at-idxs
  [chat-id text public-key]
  (log/debug "[native-module] recheck-at-idxs" {:chat-id chat-id :text text :public-key public-key})
  (let [result (.mentionRecheckAtIdxs ^js (status) chat-id text public-key)]
    (transfer-mention-result result true)))

(defn new-input-text-with-mentions
  [chat-id text primary-name]
  (log/debug "[native-module] new-input-text-with-mentions"
             {:chat-id chat-id :text text :primary-name primary-name})
  (.mentionNewInputTextWithMentions ^js (status) chat-id text primary-name))

(defn clear-mentions
  [chat-id]
  (log/debug "[native-module] clear-mentions" {:chat-id chat-id})
  (.mentionClearMentions ^js (status) chat-id))

(defn calculate-suggestions
  [chat-id text]
  (log/debug "[native-module] calculate-suggestions" {:chat-id chat-id :text text})
  (let [result (.mentionCalculateSuggestions ^js (status) chat-id text)]
    (transfer-mention-result result true)))
