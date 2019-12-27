*** Keywords ***
kw
    @{new_line1}=    Collections.Get Slice From List    0    0    12
    ${new_line2}=
    ...    BuiltIn.Evaluate    2
*** Settings ***
Library    Collections