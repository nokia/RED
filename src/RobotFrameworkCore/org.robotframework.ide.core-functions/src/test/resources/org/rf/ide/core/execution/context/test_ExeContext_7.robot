*** Settings ***
Resource  resource1.robot

*** Test Cases ***
test7
   	${var}=  KeywordReturnValue
	Log    ${var}
	${var2}=  SecondKeywordReturnValue
	Log    ${var2}

*** Keywords ***
SecondKeywordReturnValue
	Log  Return value
    [Return]  2