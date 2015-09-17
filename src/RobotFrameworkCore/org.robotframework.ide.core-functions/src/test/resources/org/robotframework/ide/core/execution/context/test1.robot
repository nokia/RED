*** Settings ***
Resource  resource1.robot
Resource  resource3.robot

*** Test Cases ***
test a
	Log  ${scalar}
	Log  1234
	
	Log  1234
	key1
	key3
	Log  1234
	MyLog2
	testK
	Log 1234
	
test b
	Log  1234
	key5
	Log  1234
	
*** Variables ***
${a}  12

*** Keywords ***
key1
	Log  1234
	Log  1234
	key2
key2
	Log  1234
	MyLog3
key3
	Log  1234
	key4
	Log  1234
key4
	Log  1234
	Log  1234
key5
	key1
	key3	
