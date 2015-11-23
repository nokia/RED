*** Variables ***
@{t}    1  2  3

*** Test Cases ***
test a
	:FOR	${i}	IN	@{t}
	\	Log	 ${i}
	\	Log  1234
	Log  1234
	testFor
	Log  1234
	LoopKeyword
	Log  1234
	
*** Keywords ***
testFor
	Log  1234
	:FOR	${i}	IN	@{t}
	#\  Log 1234
	\	Log	 ${i}
	Log  1234

*** Settings ***
Resource  resource3.robot