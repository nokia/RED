*** Test Cases ***
TestEqualityInsideToken
	Log		message=done	INFO

TestEqualityAtBottomOfTextArgument
	Log		message=	INFO
	
TestVariableAssignmentWithoutEquality
	${var}	Set Variable	10

TestVariableAssignmentWithEqualityAtComment
	${var}	Set Variable	10	#${var}=	pNONE as default

TestVariableAssignmentWithEqualityAtCommentAndPrettyAlign
	${var}	Set Variable	10	#${var} =	pNONE as default
	
TestVariableAssignmentWithEqualityAtCommentAndBreak
	${var}	Set Variable	10	#${var}		=	pNONE as default
	
TestVariableAssignmentWithEquality
	${var}=		Set Variable	10
	
TestVariableAssignmentWithEqualityAndPrettyAlign
	${var} =	Set Variable	10
	
TestVariableAssignmentWithEquality_andPipeLineSeparated
|	|	${var}=	|	Set Variable	|	10	

TestVariableAssignmentWithEqualityAndPrettyAlignDouble_andPipeLineSeparated
|	|	${var}  = |	Set Variable	|	10

TestTwoVariablesAllCombinationOfEqualityAndAssignment
	${err}	${out}			Execute Command		getOut	both

	${err}=		${out}		Execute Command		getOut	both
	${err}		${out}=		Execute Command		getOut	both

	${err}=		${out}=		Execute Command		getOut	both
	
TestTwoVariablesAllCombinationOfEqualityAndAssignment_andPipeLineSeparated
|	|	${err} 	|	${out} 	|	Execute Command	|	getOut	|	both

|	|	${err} =	|	${out} 	|	Execute Command	|	getOut	|	both
|	|	${err} 	|	${out} =	|	Execute Command	|	getOut	|	both
|	|	${err} =	|	${out} =	|	Execute Command	|	getOut	|	both
|	|	${err}  =	|	${out}  =	|	Execute Command	|	getOut	|	both

TestVariableAndPrettyAlignAtTheEndOfLine
	Log	@{x11} 
	
TestVariableAndPrettyAlignAndEqualAtTheEndOfLine
	Log	@{x11} =

TestVariableAndPrettyAlignAndEqualAndSpaceAtTheEndOfLine
	Log	@{x11} = 