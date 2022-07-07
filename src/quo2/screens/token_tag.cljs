(ns quo2.screens.token-tag
  (:require [quo.react-native :as rn]
            [quo.previews.preview :as preview]
            [reagent.core :as reagent]
            [quo2.components.token-tag :as quo2]
            [quo.design-system.colors :as colors]))

(def descriptor [{:label   "Size:"
                  :key     :size
                  :type    :select
                  :options [{:key   :big
                             :value "big"}
                            {:key   :small
                             :value "small"}]}
                 {:label   "Value:"
                  :key     :value
                  :type    :select
                  :options [{:key   0
                             :value "0"}
                            {:key   10
                             :value "10"}
                            {:key   100
                             :value "100"}
                            {:key   1000
                             :value "1000"}
                            {:key   10000
                             :value "10000"}]}

                 {:label   "Border Color:"
                  :key     :border-color
                  :type    :select
                  :options [{:key   "#00a191"
                             :value "green"}]}
                 {:label   "Custom Required Icon:"
                  :key     :required-icon
                  :type    :select
                  :options [{:key   :main-icons2/verified-dark12
                             :value "green"}]}
                 {:label   "Is Required:"
                  :key     :is-required
                  :type  :boolean}
                 {:label   "Is Purchasable:"
                  :key     :is-purchasable
                  :type  :boolean}
                 {:label   "Token:"
                  :key     :token
                  :type    :select
                  :options [{:key   "ETH"
                             :value "ETH"}
                            {:key   "SNT"
                             :value "SNT"}]}])

(defn cool-preview []
  (let [state (reagent/atom {:size :big :value 10 :token "ETH" :is-required false :is-purchasable false})]
    (fn []
      [rn/view {:margin-bottom 50
                :padding       16}
       [preview/customizer state descriptor]
       [rn/view {:padding-vertical 60
                 :align-items      :center}
        [quo2/token-tag  (merge @state {:icon-name (if (=  (get-in @state [:token]) "ETH") :main-icons2/token-logo-eth :main-icons2/token-logo-snt)})]]])))

(defn preview-token-tag []
  [rn/view {:background-color (:ui-background @colors/theme)
            :flex             1}
   [rn/flat-list {:flex                      1
                  :keyboardShouldPersistTaps :always
                  :header                    [cool-preview]
                  :key-fn                    str}]])