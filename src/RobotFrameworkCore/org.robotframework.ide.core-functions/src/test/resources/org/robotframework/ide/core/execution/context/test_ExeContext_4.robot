*** Test Cases ***
test a
	#Log  1234
	key1
	#Log  1234
	#Log  1234
	Log  1234
	#Log  1234
	
test b
	#Log  1234
	key2
	#Log  1234
	
*** Variables ***
${a}  12

*** Keywords ***
key1
	#Log  1234
	Log  1234
	#Log  1234
	key2
key2
	#Log  1234
	Log  1234
	#Log  1234
	Log  1234

