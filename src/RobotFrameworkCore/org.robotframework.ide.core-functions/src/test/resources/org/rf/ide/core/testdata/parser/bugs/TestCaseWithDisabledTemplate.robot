*** Settings ***
Test Template    Example keyword
*** Test Cases ***
test
    [Template]    NONE
    first argument    second argument
    a    b    c
    a    #comment    b    c