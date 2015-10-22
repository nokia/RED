*** Settings ***
Resource  resource1.robot

*** Test Cases ***
test a
	key1
	
test b
	key2
	
*** Variables ***
${a}  12

*** Keywords ***
key1
	Log  1234
key2
	Keyword1
	Log  1234

