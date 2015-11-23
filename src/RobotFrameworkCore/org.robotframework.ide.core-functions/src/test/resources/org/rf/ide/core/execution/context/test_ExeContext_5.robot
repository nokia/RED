*** Settings ***
Resource  resource1.robot
Test Setup  my_setup
Test Teardown  my_teardown

*** Test Cases ***
test5
   Should Be True   True
   Log  123
   Log  2

test5_2
	[Setup]  testCaseSetup
	[Teardown]  testCaseTeardown
	Log  1234
	Log  123
	
*** Keywords ***
my_setup
	Log  setup
	SetupKeyword
my_teardown
    Log  close
    Log  close2

testCaseSetup
	Log  setup
testCaseTeardown
	Log  teardown
	