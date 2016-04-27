*** Settings ***
Suite Setup  my_setup

*** Test Cases ***
test11
   Log  123
	
*** Keywords ***
my_setup
	Log  setup
	:FOR	${i}	IN	@{t}
	\	Log	 ${i}
	\	Log  ${i}

*** Variables ***
@{t}    1	