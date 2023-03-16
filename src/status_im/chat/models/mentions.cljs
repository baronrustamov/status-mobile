(ns status-im.chat.models.mentions
  (:require [clojure.string :as string]
            [quo.react :as react]
            [quo.react-native :as rn]
            [re-frame.core :as re-frame]
            [status-im.native-module.mention :as mention]
            [utils.re-frame :as rf]
            [status-im.utils.platform :as platform]
            [taoensso.timbre :as log]))

(rf/defn on-text-input
  {:events [::on-text-input]}
  [{:keys [db]} {:keys [previous-text start end] :as args}]
  (log/debug "[mentions] on-text-input args" args)
  (let [previous-text
        ;; NOTE(rasom): on iOS `previous-text` contains entire input's text. To
        ;; get only removed part of text we have cut it.
        (if platform/android?
          previous-text
          (subs previous-text start end))
        chat-id         (:current-chat-id db)
        state           (merge args {:previous-text previous-text})
        {:keys [state]} (mention/on-text-input chat-id state)]
    {:db (assoc-in db [:chats/mentions chat-id :mentions] state)}))

(rf/defn recheck-at-idxs
  [{:keys [db]} public-key]
  (let [chat-id                        (:current-chat-id db)
        text                           (get-in db [:chat/inputs chat-id :input-text])
        {:keys [input-segments state]} (mention/recheck-at-idxs chat-id text public-key)]
    (log/debug "[mentions]" {:input-segments input-segments :state state})
    {:db (-> db
             (assoc-in [:chats/mentions chat-id :mentions] state)
             (assoc-in [:chat/inputs-with-mentions chat-id] input-segments))}))

(rf/defn clear-suggestions
  [{:keys [db]}]
  (log/debug "[mentions] clear suggestions")
  (let [chat-id (:current-chat-id db)]
    {:db (update db :chats/mention-suggestions dissoc chat-id)}))

(rf/defn clear-mentions
  [{:keys [db] :as cofx}]
  (log/debug "[mentions] clear mentions")
  (let [chat-id (:current-chat-id db)]
    (mention/clear-mentions chat-id)
    (rf/merge
     cofx
     {:db (-> db
              (update-in [:chats/mentions chat-id] dissoc :mentions)
              (update :chat/inputs-with-mentions dissoc chat-id))}
     (clear-suggestions))))

(rf/defn check-selection
  {:events [::on-selection-change]}
  [{:keys [db]}
   {:keys [start end]}]
  (let [chat-id         (:current-chat-id db)
        text            (get-in db [:chat/inputs chat-id :input-text])
        {:keys [state]} (mention/check-selection chat-id text start end)]
    {:db (assoc-in db [:chats/mentions chat-id :mentions] state)}))

(re-frame/reg-fx
 ::reset-text-input-cursor
 (fn [[ref cursor]]
   (when ref
     (status/reset-keyboard-input
      (rn/find-node-handle (react/current-ref ref))
      cursor))))

(rf/defn reset-text-input-cursor
  [_ ref cursor]
  {::reset-text-input-cursor [ref cursor]})

(rf/defn calculate-suggestions
  {:events [::calculate-suggestions]}
  [{:keys [db]}]
  (let [chat-id (:current-chat-id db)
        text (get-in db [:chat/inputs chat-id :input-text])
        {:keys [mentionable-users state input-segments]}
        (mention/calculate-suggestions chat-id text)]
    {:db (-> db
             (assoc-in [:chats/mention-suggestions chat-id] mentionable-users)
             (assoc-in [:chats/mentions chat-id :mentions :at-idxs] state)
             (assoc-in [:chat/inputs-with-mentions chat-id] input-segments))}))
