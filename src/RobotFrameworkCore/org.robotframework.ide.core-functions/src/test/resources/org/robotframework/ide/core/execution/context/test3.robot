*** Variables ***
@{t}    1  2  3

*** Test Cases ***
test a
	:FOR	${i}	IN	@{t}
	\	Log	 ${i}
	\	Log  1234
	Log  1234