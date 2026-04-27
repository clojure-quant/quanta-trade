(ns quanta.trade.dali.viewer.backtest.roundtrip-chart
  (:require
   [tech.v3.dataset :as tds]
   [rtable.viewer.vega :refer [vegalite]]))

(def bar
  {;:$schema "https://vega.github.io/schema/vega-lite/v4.json"
   :description "A simple bar chart with embedded data."
   :height "800"
   :width "1200"
   :vconcat [{:height 500 ; js/null  ; Allows the line chart to take all available space
              :width "1200" ; Makes the line chart full width
              :layer [{:mark {:type "line"
                              :color "red"
                              :interpolate "step-after"
                              :tooltip {:content "data"}}
                       :encoding {:x {:field "exit-date" :type "temporal"}
                                  :y {:field "equity-gross"
                                      :type "quantitative"
                                      :domain false
                                      :scale {:zero false}}}}
                      {:mark {:type "line"
                              :interpolate "step-after"
                              :tooltip {:content "data"}}
                       :encoding {:x {:field "exit-date" :type "temporal"}
                                  :y {:field "equity"
                                      :type "quantitative"
                                      :domain false
                                      :scale {:zero false}}}}]}

             {:height "100"
              :width "1200" ; Makes the line chart full width
              :mark {:type "bar"
                     :tooltip {:content "data"}}
              :encoding {:x {:field "exit-date" :type "temporal"
                             :title "trade-pl"}
                         :y {:field "pl" :type "quantitative"
                             :title "trade-pl"}}}
             {:height "100"
              :width "1200" ; Makes the line chart full width
              :mark {:type "line"
                     :interpolate "step-after"
                     :tooltip {:content "data"}}
              :encoding {:x {:field "exit-date" :type "temporal"
                             :title "drawdown prct"}
                         :y {:field "drawdown-prct" :type "quantitative"
                             :title "drawdown prct"
                             :scale {; "domain": [-1, 4], 
                                     ;:reverse true ; this crashes.
                                     }}}}
             {:height "200"
              :width "1200" ; Makes the line chart full width
              :mark {:type "point"
                     :color "blue"
                     :tooltip {:content "data"}}
              :encoding {:x {:field "exit-date" :type "temporal"}
                         :y {:field "trade-no"
                             :type "quantitative"
                             :axis {:title "trade count" :orient "left"}}}}]

   :data {:name "table"}})

(defn transform [ds]
  (->> (tds/rows ds)
       (into [])))

(defn roundtrip-chart [ds]
  (if ds
    [vegalite {:spec bar :data {:table (transform ds)}}]
    [:p "loading data.."]))