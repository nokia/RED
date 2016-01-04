*** Settings ***
Resource  resource1.robot
Resource  ${Release}/resource1.robot

*** Test Cases ***
test8
	Log  start
	MyKeyword1
	SetupKeyword
	testN

*** Variables ***
${Release}=    resources
