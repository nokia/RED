*** Settings ***
Resource  resource2.robot

*** Keywords ***
MyLog2
	Log  some info
	Log  1234
	MyLog
	testK
	
testK
	Log  1234
	Log  1234
	MyLog
	testM
	
testM
	Log  1234
	Log  1234
	
Keyword1
	Keyword2
	Log  1234
	
SetupKeyword
	Log  12345
	Log  123