*** Settings ***
Resource  resource1.robot
Resource  resources/resource1.robot

*** Test Cases ***
test8
	Log  start
	MyKeyword1
	SetupKeyword
	testN
	