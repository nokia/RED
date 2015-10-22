*** Settings ***
Suite Setup  my_setup
Suite Teardown  my_teardown

*** Test Cases ***
test6
   Log  123
   Log  2
	
*** Keywords ***
my_setup
	Log  setup
	Log  setup2
my_teardown
    Log  close
    Log  close2

	