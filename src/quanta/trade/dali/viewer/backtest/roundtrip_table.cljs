(ns quanta.trade.dali.viewer.backtest.roundtrip-table
  (:require
   [rtable.viewer.cheetah :refer [cheetah-ds]]
   ["cheetah-grid" :as cheetah-grid :refer [columns]]
   [quanta.trade.dali.format :as f]
   [quanta.trade.dali.cheetah-style :refer [blue-color]]))

; non displayed columns:
;
; :volume-trading	:cum-log :volume-exit	:equity
;	 :cum-prct	:pl-log	:pl-gross		:pl-points	
;  :cum-points	

 (def ButtonColumn
   (-> columns .-type .-ButtonColumn))
 
 (def ButtonAction
   (-> columns .-action .-ButtonAction))

 (defn cols-link [action]
   [{:caption "..."
     :width 50
     :columnType (ButtonColumn. (clj->js {:caption "c"}))
     :action (ButtonAction. (clj->js {:action (fn [r]
                                                (let [r (js->clj r)]
                                                  (println "clicked: " r)
                                                  (action {:date (or (:entry-date r) (get r "entry-date"))
                                                           :asset (or (:asset r) (get r "asset"))})))}))}])
(def cols-main 
  [; bar
   {:field "asset" :caption "asset" :width 90}
                ;{:field "id" :caption "id" :width 50}
   {:field "side" :caption "side" :width 50}
   {:field "qty" :caption "qty" :width 50}
   {:field "volume-entry" :caption "vol" :width 50 :format f/nr-format-0-digits}
                ; entry
   {:field "entry-date" :caption "entry-dt" :width 160
    :format f/dt-yyyymmdd-hhmm}
   ;{:field "entry-idx" :caption "entry-idx" :width 50 :style {:bgColor "#5f5"}}
   {:field "entry-price" :caption "entry-p" :width 90 #_:style #_'demo.page.cheetah/red-color
    :format f/nr-format-auto}
                ; exit
   {:field "exit-date" :caption "exit-dt" :width 160
    :format f/dt-yyyymmdd-hhmm}
   ;{:field "exit-idx" :caption "exit-idx" :width 50 :style {:bgColor "#5f5"}}
   {:field "exit-price" :caption "exit-p" :width 50
    :format f/nr-format-auto}
   {:field "exit-reason" :caption "exit-reason" :width 90}
                 ; pl
   {:field "pl" :caption "pl" :width 50 :format f/nr-format-0-digits}
   {:field "pl-prct" :caption "pl%" :width 50 :format f/nr-format :format-args ["%.1f"]}
   {:field "fee" :caption "fee" :width 50 :format f/nr-format-0-digits}
   {:field "equity" :caption "equity" :width 50 :format f/nr-format-0-digits}
   {:field "drawdown" :caption "drawdown" :width 50 :format f/nr-format-0-digits}
   {:field "drawdown-prct" :caption "ddl%" :width 50 :format f/nr-format-0-digits}
                ; metrics
   {:field "bars" :caption "bars" :width 50}
   {:field "win?" :caption "win?" :width 50 :style blue-color :format f/format-bool}]
  ) 


(defn roundtrips-cheetah 
  ([ds]
    [cheetah-ds
     {:style {:width "100%" :height "100%"}
      :columns  cols-main
      :ds ds}])
  ([ds action]
    [cheetah-ds
     {:style {:width "100%" :height "100%"}
      :columns (if action 
                 (concat (cols-link action) cols-main)
                 cols-main)
      :ds ds}]))
