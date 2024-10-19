# exit signals


## label
- the exit-rule that triggers the exit gets logged in the backtest
  in the :exit-reason column. 
- each exit rule has a type and this is the default label
- if you have more than one exit-rule of the same type (this could make
  sense if you want to have two trailing rules that work with offset, so
  you dont know which rule is higher/lower), you want to know which rule
  was which.
- so the label overrides the :type.


## :trailing-stop-offset 
- the exit-level is changing over time with price aciton
  - on position open the exit-level is *entry-price +- col*
  - on new bar the new exit level might become *close +- col*.
    note that the exit-level can only become better, so 
    meaning the stop gets moved more and more to profit.
- parameter
  - col: a column in the dataset produced by the the algo
  - label: an optional label
