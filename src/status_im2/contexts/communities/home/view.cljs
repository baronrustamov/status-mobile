(ns status-im2.contexts.communities.home.view
  (:require [utils.i18n :as i18n]
            [quo2.core :as quo]
            [reagent.core :as reagent]
            [react-native.core :as rn]
            [status-im2.common.home.view :as common.home]
            [status-im2.contexts.communities.menus.community-options.view :as options]
            [utils.re-frame :as rf]
            [react-native.safe-area :as safe-area]
            [react-native.blur :as blur]
            [quo2.foundations.colors :as colors]
            [status-im2.contexts.communities.home.style :as style]
            [react-native.platform :as platform]))

(defn item-render
  [{:keys [id] :as item}]
  (let [unviewed-counts (rf/sub [:communities/unviewed-counts id])
        item            (merge item unviewed-counts)]
    [quo/communities-membership-list-item
     {:style         {:padding-horizontal 18}
      :on-press      #(rf/dispatch [:navigate-to-nav2 :community-overview id])
      :on-long-press #(rf/dispatch
                       [:bottom-sheet/show-sheet
                        {:content       (fn []
                                          [options/community-options-bottom-sheet id])
                         :selected-item (fn []
                                          [quo/communities-membership-list-item {} item])}])}
     item]))

(def tabs-data
  [{:id :joined :label (i18n/label :chats/joined) :accessibility-label :joined-tab}
   {:id :pending :label (i18n/label :t/pending) :accessibility-label :pending-tab}
   {:id :opened :label (i18n/label :t/opened) :accessibility-label :opened-tab}])

(defn home
  []
  (let [selected-tab (reagent/atom :joined)]
    (fn []
      (let [{:keys [joined pending opened]} (rf/sub [:communities/grouped-by-status])
            selected-items                  (case @selected-tab
                                              :joined  joined
                                              :pending pending
                                              :opened  opened)]
        [safe-area/consumer
         (fn [{:keys [top]}]
           [:<>
            [rn/flat-list
             {:key-fn                            :id
              :content-inset-adjustment-behavior :never
              :header                            [rn/view {:height (+ 245 top)}]
              :render-fn                         item-render
              :data                              selected-items}]
            [rn/view
             {:style (style/blur-container top)}
             [blur/view
              {:blur-amount (if platform/ios? 20 10)
               :blur-type   (if (colors/dark?) :dark (if platform/ios? :light :xlight))
               :style       style/blur}]
             [common.home/top-nav]
             [common.home/title-column
              {:label               (i18n/label :t/communities)
               :handler             #(rf/dispatch [:bottom-sheet/show-sheet :add-new {}])
               :accessibility-label :new-chat-button}]
             [quo/discover-card
              {:on-press            #(rf/dispatch [:navigate-to :discover-communities])
               :title               (i18n/label :t/discover)
               :description         (i18n/label :t/whats-trending)
               :accessibility-label :communities-home-discover-card}]
             [quo/tabs
              {:size           32
               :style          style/tabs
               :on-change      #(reset! selected-tab %)
               :default-active @selected-tab
               :data           tabs-data}]]])]))))
