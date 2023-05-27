### How to interact with the system:

##### Set the temperature:
- t [temperature as double] </br>
-> unit: celsius... the unit is hardcoded in the UI but could be changed. If unit is changed, the threshold value activating and deactivating aircondition at 20°C has to be changed

<i>Example: t 19.5 celsius</i>



##### Turn Air Condition on and off:
- a power <br/>
-> switches air condition on or off depending on the current state. If power is off, the air condition will ignore the messages from the temperature sensor


##### Interaction with fridge:
- f display <br/>
-> shows the current items in the fridge and their amount 
- f history <br/>
-> shows the order history

- f consume [itemname] <br/>
-> consumes on item from the fridge. If the item is not available, the user will be notified <br/>
-> Available items on start: {BEER=2, SALAD=2, MILK=2, CHEESE=2} <br/>
-> <i>Example: f consume beer</i>

- f order [itemname] <br/>
-> orders one item from the fridge. If the item can not be ordered, the user will be notified <br/>
-> items which can be ordered: beer, salad, milk, cheese <br/>
- <i>Example: f order beer</i>

##### Interaction with media player:
- m play <br/>
- m stop <br/>



