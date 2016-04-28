| *** Settings *** |
Library		MyLib		WITH NAME	ok
Library		MyLib2
...			param1
...			WITH NAME
...			ok2
 |	Library	|	ok


| *** Variables *** |
| ${x}	|	|	| z |	c
...		nowy
# du2

${var_dodaj}






${var_dodaj2}

| *** Variables *** | *** value *** |
&{map}		value=key	value2=key2
${var}	value1	#comment
${var2} = 
...		value2	value3	#comment
| ...	|	value4	|	value5	|	#comment
${var}  xyz
| *** Variables *** | *** value_new *** |
${var1_new}	value1	#comment
${var2_new}	
...		value2	value3	#comment
| ...	|	value4	|	value5	|	#comment