(ns status-im2.contexts.communities.discover.featured-info-sheet
  (:require [quo2.core :as quo]
            [react-native.core :as rn]
            [utils.re-frame :as rf]
            [utils.i18n :as i18n]))

(defn sheet-info
  []
  [rn/view {:flex               1
            :padding-horizontal 20}
   [quo/text
    {:accessibility-label :featured-info-title
     :weight              :semi-bold
     :size                :heading-2
     :style               {:margin-bottom 12}}
    "Featured Communities"]
   [quo/text
    {:accessibility-label :featured-info-description
     :weight              :regular
     :size                :paragraph-2
     :style               {:margin-bottom 12}}
    "Here will be something relevant about this topic. This will help the user get more context and therefore have a better understanding of it."]
   [quo/button
    {:on-press #(rf/dispatch [:bottom-sheet/hide])
     :type     :outline}
    (i18n/label :t/read-more)]])