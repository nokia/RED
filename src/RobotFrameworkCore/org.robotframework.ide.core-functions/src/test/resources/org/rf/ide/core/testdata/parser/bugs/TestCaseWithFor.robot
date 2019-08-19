*** Test Cases ***
test case
    [Template]    Example keyword
    FOR    ${item}    IN    @{ITEMS}
        ${item}    2nd_arg    3rd
        ${item}    2nd_arg    #comments
    END
    FOR    ${index}    IN RANGE    42
    \    ${index}    4    4    4    #comments
    \    ${index}    3    3    #comments
    \    ${index}    2
    \    ${index}
