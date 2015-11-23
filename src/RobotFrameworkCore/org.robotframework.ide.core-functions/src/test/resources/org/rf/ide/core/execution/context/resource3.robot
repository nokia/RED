*** Keywords ***
MyLog3
	Log  1234
	Log  1234
	testP
	
testP
	Log  1234
	Log  1234

Keyword3
	Log  1234
	Log  1234
	Keyword4
	
Keyword4
	Log  1234
	
LoopKeyword
	:FOR	${i}	IN	2
	\	Log	 ${i}
	\	Log  1234
	\	Log  1234