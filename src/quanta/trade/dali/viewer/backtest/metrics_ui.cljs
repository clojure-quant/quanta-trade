(ns quanta.trade.dali.viewer.backtest.metrics-ui
  (:require
   [quanta.trade.dali.format :as f]))

(defn to-fixed [n d]
  (.toFixed n d))

(defn round-number-digits
  [digits number] ; digits is first parameter, so it can easily be applied (data last)
  (try
    (if (nil? number) "" (to-fixed number digits))
    (catch js/Error e
      (println "round-numnber-digits exception: " e)
      number)))

(defn td-border [& children]
  (into [:td {:style {:border "1px solid"
                      :padding "5px"}}]
        children))

(defn metrics-view [{:keys [class style roundtrip nav opts]
                     :or {class "w-full h-full"
                          style {}} :as all}]
  (let [{:keys [pf all win loss]} roundtrip
        {:keys [equity-final cum-pl fee-total
                max-drawdown max-drawdown-prct]} nav]
    [:div {:class class :style style}
     [:div "options"
      [:p (pr-str opts)]]
     [:table
      [:tbody
       [:tr
        [:td "equity final"]
        [:td (f/nr-format-0-digits equity-final)]]
       [:tr
        [:td "cum-pl"]
        [:td (f/nr-format-0-digits  cum-pl)]]
       [:tr
        [:td "max-dd"]
        [:td (f/nr-format-0-digits max-drawdown) " prct: " (f/nr-format-0-digits max-drawdown-prct)]]
       [:tr
        [:td "profit factor "]
        [:td pf]]
       [:tr
        [:td "fees"]
        [:td  (f/nr-format-0-digits fee-total)]]]]
     [:table
      [:tbody
       [:tr
        [td-border [:span {:style {:width "3cm"}} " "]]
        [td-border [:span {:style {:min-width "100px"}} "all"]]
        [td-border [:span {:style {:width "100px"}} "win"]]
        [td-border [:span {:style {:width "100px"}} "loss"]]]
       [:tr
        [td-border "#trades"]
        [td-border (:trades all) [:span {:class "text-blue-500"
                                         :style {:float "right"}}
                                  (-> all :trade-prct f/nr-format-0-digits)]]
        [td-border (:trades win) [:span {:class "text-blue-500"
                                         :style {:float "right"}}
                                  (-> win :trade-prct f/nr-format-0-digits)]]
        [td-border (:trades loss) [:span {:class "text-blue-500"
                                          :style {:float "right"}}
                                   (-> loss :trade-prct f/nr-format-0-digits)]]]
       [:tr
        [td-border "pl"]
        [td-border (-> all :pl f/nr-format-0-digits)]
        [td-border (-> win :pl f/nr-format-0-digits)]
        [td-border (-> loss :pl f/nr-format-0-digits)]]
       [:tr
        [td-border "mean pl"]
        [td-border (-> all :pl-mean f/nr-format-auto)]
        [td-border (-> win :pl-mean f/nr-format-auto)]
        [td-border (-> loss :pl-mean f/nr-format-auto)]]
       [:tr
        [td-border "median pl"]
        [td-border (-> all :pl-median f/nr-format-auto)]
        [td-border (-> win :pl-median f/nr-format-auto)]
        [td-border (-> loss :pl-median f/nr-format-auto)]]
       [:tr
        [td-border "bars avg [total]"]
        [td-border (-> all :bar-avg f/nr-format-0-digits)
         [:span {:class "text-blue-500"
                 :style {:float "right"}}
          (str "[" (:bars all) "]")]]
        [td-border (-> win :bar-avg f/nr-format-0-digits)
         [:span {:class "text-blue-500"
                 :style {:float "right"}}
          (str "[" (:bars win) "]")]]
        [td-border (-> loss :bar-avg f/nr-format-0-digits)
         [:span {:class "text-blue-500"
                 :style {:float "right"}}
          (str "[" (:bars loss) "]")]]]]]]))

