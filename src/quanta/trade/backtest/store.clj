(ns quanta.trade.backtest.store
  (:require
   [nano-id.core :refer [nano-id]]
   [tech.v3.libs.clj-transit :as tech-transit]
   [tech.v3.io :as io]))

(defn ds->transit-json-file
  [fname ds]
  (with-open [outs (io/output-stream! fname)]
    (tech-transit/dataset->transit ds outs :json tech-transit/java-time-write-handlers)))

(defn transit-json-file->ds
  [fname]
  (with-open [ins (io/input-stream fname)]
    (tech-transit/transit->dataset ins :json tech-transit/java-time-write-handlers)))

(defn ds->nippy [filename ds]
  (let [s (io/gzip-output-stream! filename)]
    (io/put-nippy! s ds)))

(defn nippy->ds [filename]
  (let [s (io/gzip-input-stream filename)
        ds (io/get-nippy s)]
    ds))

(defn save-ds-transit-safe [fname ds]
  (let [fname-tmp "/tmp/ds.nippy.gz"
        _ (ds->nippy fname-tmp ds)
        ds-safe (nippy->ds fname-tmp)]
    (ds->transit-json-file fname ds-safe)))