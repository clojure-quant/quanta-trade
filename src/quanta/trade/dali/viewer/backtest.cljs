(ns quanta.trade.dali.viewer.backtest
  (:require
   [container :refer [tab]]
   [rtable.rtable]
   [quanta.trade.dali.format]
   [quanta.trade.dali.viewer.backtest.roundtrip-table]
   [quanta.trade.dali.viewer.backtest.roundtrip-chart]
   [quanta.trade.dali.viewer.backtest.metrics-ui]))

(def table
  quanta.trade.dali.viewer.backtest.roundtrip-table/roundtrips-cheetah)

(def metrics
  quanta.trade.dali.viewer.backtest.metrics-ui/metrics-view)

(def chart
  quanta.trade.dali.viewer.backtest.roundtrip-chart/roundtrip-chart)


(defn backtest [{:keys [style class data action]
                 :or {style {:height "100%"
                             :width "100%"}
                      class ""}
                 :as opts}]
  [tab {:class class
        :style style}
   "metrics"
   [quanta.trade.dali.viewer.backtest.metrics-ui/metrics-view (:metrics data)]
   "chart"
   [quanta.trade.dali.viewer.backtest.roundtrip-chart/roundtrip-chart (:roundtrip-ds data)]
   "roundtrips"
   [quanta.trade.dali.viewer.backtest.roundtrip-table/roundtrips-cheetah (:roundtrip-ds data) action]])

(defn make-render-id  [show-backtest]
  (fn [col-info row]
    (let [id (:label row)]
      [:div
       [:a {:display {:cursor "pointer"
                      :margin "1px"}
            :on-click #(when show-backtest
                         (show-backtest id) 
                         )}
        [:i {:class "fas fa-eye m-1"}]
        [:span {:style {:background-color "blue"}}
         id]]])))

(defn overview [{:keys [result show-backtest action]}]
  [rtable.rtable/rtable
   {:class "table-head-fixed padding-sm table-blue table-striped table-hover"
    :style {:width "100%"
            :height "100%"
            :border "3px solid green"}}
   [{:path :label :header "asset" :render-cell (make-render-id show-backtest)}
    {:path :trades}
    {:path :profit-factor :format quanta.trade.dali.format/nr-format-auto}
    {:path :equity-final :format quanta.trade.dali.format/nr-format-0-digits}]
   result])


