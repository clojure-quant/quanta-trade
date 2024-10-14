# quanta-trade [![GitHub Actions status |clojure-quant/quanta-trade](https://github.com/clojure-quant/quanta-trade/workflows/CI/badge.svg)](https://github.com/clojure-quant/quanta-trade/actions?workflow=CI)[![Clojars Project](https://img.shields.io/clojars/v/io.github.clojure-quant/quanta-trade.svg)](https://clojars.org/io.github.clojure-quant/quanta-trade)


## What is quanta-trade?

### trade-manager
- gets a entry/exit rule config.
- input is a stream of entry-signals
- is in contact with brokers (or simulated brokers)
- opens and closes positions based on the entry stream, the position-update-stream 
  and its rule-config.

### quanta.trade.report 
 - input is a list of open and closed positions ("roundtrips")
 - outputs
   - nav time-series (so how did the portfolio value develop over time) 
   - metrics (like profit-factor and maximum drawdown) 




## for developers

### for development
 start a nrepl-connection to the dev project.
 in dev folder you will find plenty of namespaces to play with


*code linter*  `clj -M:lint`

*code formatter `clj -M:cljfmt-fix`

*unit tests* `clj -M:test`









