*** Keywords ***
Empty

| EmptyWithPipeAtBeginning

| EmptyWithPipeAtBeginningAndEnd |

| EmptyWithPipeAtBeginningAndEndAndSpaceAfter | 

| 	EmptyWithPipeAtBeginningAndEndAndSpaceAfterAndTabs | 	

| One_ExecRowOnly_WithSpaces |
  Log  ok

| One_ExecRowOnly_WithSpacesAndSpaceAsLast |
  Log  ok 

| One_ExecRowOnly_WithSpacesAndSpaceAsLastAndTab |
  Log  ok 	 

| One_ExecRowOnly_WithSpacesAndSpaceAsLastAndTab_Pipe |
 |  | Log | ok |	 

| Three_ExecsRowOnly_WithSpacesAndSpaceAsLastAndTab_Pipe |
 |  | Log | ok |	 
 | ... | data | 
  ...  not
  Log  ok2
  Log  ok3
