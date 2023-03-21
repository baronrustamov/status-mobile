(ns status-im2.contexts.onboarding.create-password.view
  (:require
    [quo2.core :as quo]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im2.common.resources :as resources]
    [status-im2.contexts.onboarding.common.navigation-bar.view :refer [navigation-bar]]
    [status-im2.contexts.onboarding.create-password.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn background-image
  []
  [rn/image
   {:style       style/image-background
    :blur-radius 13
    :source      (resources/get-image :onboarding-blur-bg)}])

(defn header
  []
  [rn/view {:style style/heading}
   [quo/text
    {:style  style/heading-title
     :weight :semi-bold
     :size   :heading-1}
    (i18n/label :t/password-creation-title)]
   [quo/text
    {:style  style/heading-subtitle
     :weight :regular
     :size   :paragraph-1}
    (i18n/label :t/password-creation-subtitle)]])

(defn password-with-hint
  [{{:keys [text status shown]} :hint :as input-props}]
  [rn/view
   [quo/input
    (-> input-props
        (dissoc :hint)
        (assoc :type           :password
               :override-theme :dark
               :blur?          true))]
   [rn/view {:style style/label-container}
    (when shown
      [:<>
       [quo/icon (if (= status :success) :i/check-circle :i/info)
        {:container-style style/label-icon
         :color           (style/label-icon-color status)
         :size            16}]
       [quo/text
        {:style (style/label-color status)
         :size  :paragraph-2}
        text]])]])

(defn password-inputs
  [{:keys [passwords-match? on-change-password on-change-repeat-password on-input-focus
           password-long-enough? empty-password? same-password-length?]}]
  (reagent/with-let [show-hint-2? (reagent/atom false)]
    (let [_ (when same-password-length? (reset! show-hint-2? true))
          hint-1-status (if password-long-enough? :success :neutral)
          hint-2-status (if passwords-match? :success :danger)
          hint-2-text   (if passwords-match?
                          (i18n/label :t/password-creation-match)
                          (i18n/label :t/password-creation-dont-match))
          error?        (and @show-hint-2?
                             (not passwords-match?)
                             (not empty-password?))]
      [:<>
       [password-with-hint
        {:hint           {:text   (i18n/label :t/password-creation-hint)
                          :status hint-1-status
                          :shown  true}
         :placeholder    (i18n/label :t/password-creation-placeholder-1)
         :on-change-text on-change-password
         :on-focus       #(on-input-focus :password)}]
       [rn/view {:style style/space-between-inputs}]
       [password-with-hint
        {:hint           {:text   hint-2-text
                          :status hint-2-status
                          :shown  (and (not empty-password?)
                                       @show-hint-2?)}
         :error?         error?
         :placeholder    (i18n/label :t/password-creation-placeholder-2)
         :on-change-text on-change-repeat-password
         :on-focus       #(on-input-focus :repeat-password)
         :on-blur        #(if empty-password?
                            (reset! show-hint-2? false)
                            (reset! show-hint-2? true))}]])))

(def strength-status
  {1 :very-weak
   2 :weak
   3 :okay
   4 :strong
   5 :very-strong})

(defn help
  [{{:keys [lower-case? upper-case? numbers? symbols?]} :validations
    password-strength                                   :password-strength}]
  [rn/view
   [quo/strength-divider {:type (strength-status password-strength :info)}
    (i18n/label :t/password-creation-tips-title)]
   [rn/view {:style style/password-tips}
    [quo/tips {:completed? lower-case?}
     (i18n/label :t/password-creation-tips-1)]
    [quo/tips {:completed? upper-case?}
     (i18n/label :t/password-creation-tips-2)]
    [quo/tips {:completed? numbers?}
     (i18n/label :t/password-creation-tips-3)]
    [quo/tips {:completed? symbols?}
     (i18n/label :t/password-creation-tips-4)]]])

(defn has-lower-case [s] (re-find #"[a-z]" s))
(defn has-upper-case [s] (re-find #"[A-Z]" s))
(defn has-numbers [s] (re-find #"\d" s))
(defn has-symbols [s] (re-find #"[^a-zA-Z0-9\s]" s))
(defn at-least-10-chars? [s] (>= (count s) 10))

(defn password-validations
  [s]
  (->> s
       ((juxt has-lower-case has-upper-case has-numbers has-symbols at-least-10-chars?))
       (map boolean)
       (zipmap [:lower-case? :upper-case? :numbers? :symbols? :long-enough?])))

(defn calc-password-strength
  [validations]
  (->> (vals validations)
       (filter true?)
       (count)))

(defn password-form
  [{:keys [scroll-to-end-fn]}]
  (reagent/with-let [state               (reagent/atom {:password            ""
                                                        :repeat-password     ""
                                                        :accepts-disclaimer? false
                                                        :focused-input       nil})
                     password            (reagent/cursor state [:password])
                     repeat-password     (reagent/cursor state [:repeat-password])
                     accepts-disclaimer? (reagent/cursor state [:accepts-disclaimer?])
                     focused-input       (reagent/cursor state [:focused-input])]
    (let [{:keys [long-enough?]
           :as   validations} (password-validations @password)
          password-strength   (calc-password-strength validations)
          empty-password?     (empty? @password)
          same-passwords?     (= @password @repeat-password)
          same-length?        (and (not empty-password?)
                                   (= (count @password) (count @repeat-password)))
          meet-requirements?  (and (not empty-password?)
                                   (= password-strength 5)
                                   same-passwords?
                                   @accepts-disclaimer?)]
      [:<>
       [rn/view {:style style/top-part}
        [header]
        [password-inputs
         {:password-long-enough?     long-enough?
          :passwords-match?          same-passwords?
          :empty-password?           empty-password?
          :same-password-length?     same-length?
          :on-input-focus            (fn [input-id]
                                       (scroll-to-end-fn)
                                       (reset! focused-input input-id))
          :on-change-password        #(reset! password %)
          :on-change-repeat-password #(reset! repeat-password %)}]]

       [rn/view {:style style/bottom-part}
        (when (= @focused-input :password)
          [help
           {:validations       validations
            :password-strength password-strength}])

        (when (= @focused-input :repeat-password)
          [rn/view {:style style/disclaimer-container}
           [quo/disclaimer
            {:on-change #(reset! accepts-disclaimer? %)
             :checked?  @accepts-disclaimer?}
            (i18n/label :t/password-creation-disclaimer)]])

        [rn/view {:style style/button-container}
         [quo/button
          {:disabled (not meet-requirements?)
           :on-press #(rf/dispatch [:navigate-to :enable-biometrics])}
          (i18n/label :t/password-creation-confirm)]]]])))

(defn create-password
  []
  (reagent/with-let [scroll-view-ref  (atom nil)
                     scroll-to-end-fn #(js/setTimeout (.-scrollToEnd @scroll-view-ref)
                                                      250)]
    [rn/view
     [background-image]
     [rn/scroll-view
      {:ref                     #(reset! scroll-view-ref %)
       :style                   style/overlay
       :content-container-style style/content-style}
      [navigation-bar {:on-press-info #(js/alert "Info pressed")}]
      [password-form {:scroll-to-end-fn scroll-to-end-fn}]]]))
