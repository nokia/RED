*** Settings ***
Library   OperatingSystem 

 *** Test Cases ***
test1
    [Documentation]    doc1    
    Import Resource    path
    None Shall Pass    who
    Repeat Keyword    repeat    name    
    
test2
    [Arguments]    ${a}  ${b}
    [Tags]    new    
    Catenate2    a  b  c
    
# test3
    # Log  4
    # log  5
    
    # log  7