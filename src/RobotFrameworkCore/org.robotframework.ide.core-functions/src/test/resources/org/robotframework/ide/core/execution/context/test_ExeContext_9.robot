*** Variables ***
@{t}    1

*** Test Cases ***
test a
	:FOR	${i}	IN	@{t}
	\	Log	 ${i}
	\   testFor
	\	Log  ${i}
	# Log  comment
	Log  end
	
*** Keywords ***
testFor
	Log  start
	:FOR	${i}	IN	@{t}
	#\  Log 1234
	\	Log	 ${i}
	Log  keyword_end