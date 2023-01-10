(ns status-im2.contexts.chat.lightbox.top-view
  (:require
    [quo2.core :as quo]
    [quo2.foundations.colors :as colors]
    [react-native.core :as rn]
    [react-native.orientation :as orientation]
    [react-native.platform :as platform]
    [react-native.reanimated :as ra]
    [status-im2.contexts.chat.lightbox.style :as style]
    [utils.datetime :as datetime]
    [utils.re-frame :as rf]))

(def ^:const top-view-height 56)

(defn animate-rotation
  [result screen-width screen-height insets-atom
   {:keys [rotate top-view-y top-view-x top-view-width top-view-bg]}]
  (let [top-x (+ (/ top-view-height 2) (:top insets-atom))]
    (cond
      (= result orientation/landscape-left)
      (do
        (ra/animate rotate "90deg")
        (ra/animate top-view-y 60)
        (ra/animate top-view-x (- (/ screen-height 2) top-x))
        (ra/animate top-view-width screen-height)
        (ra/animate top-view-bg colors/neutral-100-opa-70))
      (= result orientation/landscape-right)
      (do
        (ra/animate rotate "-90deg")
        (ra/animate top-view-y (- (- screen-width) 4))
        (ra/animate top-view-x (+ (/ screen-height -2) top-x))
        (ra/animate top-view-width screen-height)
        (ra/animate top-view-bg colors/neutral-100-opa-70))
      (= result orientation/portrait)
      (do
        (ra/animate rotate "0deg")
        (ra/animate top-view-y 0)
        (ra/animate top-view-x 0)
        (ra/animate top-view-width screen-width)
        (ra/animate top-view-bg colors/neutral-100-opa-0)))))

(defn top-view
  [{:keys [from timestamp]} insets index animations landscape? screen-width]
  [:f>
   (fn []
     (let [display-name (first (rf/sub [:contacts/contact-two-names-by-identity from]))
           bg-color     (if landscape? colors/neutral-100-opa-70 colors/neutral-100-opa-0)]
       [ra/view
        {:style (style/top-view-container (:top insets) animations screen-width bg-color)}
        [rn/view
         {:style {:flex-direction :row
                  :align-items    :center}}
         [rn/touchable-opacity
          {:on-press (fn []
                       (ra/animate (:opacity animations) 0)
                       (rf/dispatch (if platform/ios?
                                      [:chat.ui/exit-lightbox-signal @index]
                                      [:navigate-back])))
           :style    style/close-container}
          [quo/icon :close {:size 20 :color colors/white}]]
         [rn/view {:style {:margin-left 12}}
          [quo/text
           {:weight :semi-bold
            :size   :paragraph-1
            :style  {:color colors/white}} display-name]
          [quo/text
           {:weight :medium
            :size   :paragraph-2
            :style  {:color colors/neutral-40}} (datetime/to-short-str timestamp)]]]
        [rn/view {:style style/top-right-buttons}
         [rn/touchable-opacity
          {:active-opacity 1
           :style          (merge style/close-container {:margin-right 12})}
          [quo/icon :share {:size 20 :color colors/white}]]
         [rn/touchable-opacity
          {:active-opacity 1
           :style          style/close-container}
          [quo/icon :options {:size 20 :color colors/white}]]]]))])
