*** Settings ***
Resource  resource1.robot

*** Test Cases ***
test7
   	${var}=  KeywordReturnValue
	Log    ${var}
