# quanta-trade 

A typical config looks like this

``` 

{:portfolio {:fee 0.05 ; per trade in percent
             :equity-initial 10000.0}
 :entry {:type :fixed-amount :fixed-amount 20000.0}
 :exit [{:type :trailing-stop-offset :col :atr}
        {:type :trailing-profit-level :col-long :b1-mid :col-short :b1-mid}
        {:type :stop-prct :prct 5.0}
        {:type :profit-prct :prct 8.0}
        {:type :time :max-bars 50}]}

```

*What does this mean?*
- you start with 10000 initial value
- you pay 0.05% per trade, so open and close 0.1%
  - transaction cost are part of life. live with it.
  - if you want to see how big the impact is on a backtest, go 
    to metrics and there you see the total fees paid. 
  - if you want to trick yourself, you can set transaction cost to 0.0%.
    But this is a lie; use it carefully.
- when you open a position you open at 20000, so you use leverage.
- you are allowed to open one position only
- an open position is managed with 5 exit rules (in this example)
  - there are 3 types of exit rules: 
    - profit
    - loss
    - time
  - profit rules and loss rules have a price at which they will close
    the position. This price can be either static or change over time 
    (this depends on the type)
  - when there are multiple profit and loss rules, then the one with 
    the tightest price wins.
  - the time rule always fires when a position has been open for 
    too long.    

# exit signals

## how does it work?
- the position gets opened on the closing price of the bar that gives
  the entry signal (if it can be opened: it might have already a 
  position, or the portfolio might be maxed out)
- on each bar in which the position is open, all defined exit rules
  will be checked. 
- in backtest we have one problem: on a given bar, it might happen
  that both a profit and a loss rule gets triggered. In this case 
  the loss rule wins. This is because it is more conservative, and
  we have no way of knowing which one would be fired first. In realtime
  we do not ahve this issue. In code exit rules have priority: 1 for
  loss 2 for profit 3 for time. If a loss or profit rule triggers togehter
  with a time rule, then the profit or loss rule wins.

## :trailing-stop-offset 
- the exit-level is changing over time with price action
  - on position open the exit-level is *entry-price +- col*
  - on new bar the new exit level might become *close +- col*.
    note that the exit-level can only become better, so 
    meaning the stop gets moved more and more to profit.
  - on each bar, it will FIRST apply existing stops, and after
    all rules have been checked, it will modify the exit-rules.
    So todays dataset columns will drive tomorrows levels. On 
    bars there is no other way, because when the algo calculates 
    a new level, it can use all the data of the bar up to the closing
    price.  
- parameter
  - col: a column in the dataset produced by the the algo
  - label: an optional label
- a good use for this rule is to have a stop that is x*ATR away from
  the entry of the position. The more the position moves to profit, 
  the more the trailing stop will lock in accumulated profits.  

## :trailing-profit-level
- Here we have a profit target for our trade, but this target gets
  calculated from the algo and can therefore change with each bar. 
- the profit level that is being used comes for a long position from 
  the :col-long column (in this case b1-mid) and for a short position 
  from :col-short column (in this case also b1-mid).
- It makes sense to use this rule in the bollinger strategy where 
  we exit always at the mid band.   

 ## label
- the exit-rule that triggers the exit gets logged in the backtest
  in the :exit-reason column. 
- each exit rule has a type and this is the default label
- if you have more than one exit-rule of the same type (this could make
  sense if you want to have two trailing rules that work with offset, so
  you dont know which rule is higher/lower), you want to know which rule
  was which.
- so the label overrides the :type.