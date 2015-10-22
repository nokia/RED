*** Settings ***
Test Setup  s1
Test Teardown  t1
Suite Setup  s1
Suite Teardown  t1
Library  libTest
Default Tags  TAG_DEF
*** Variables ***
${t}  TG

*** Test Cases ***
testAA
    Log  4
    Run Keyword  Log  1
    
    Log  65
    Log  3454
    Log  12434
testC
    [Tags]  TAG12-${t}
    [Timeout]  2s
    [Setup]  s1
    [Teardown]  t1
    Log  test
testA
    Log  12
    
testB
    Log  42
    
testD
    Log  98
 
*** Keywords ***
s1
    Log  s
t1  
    Log  t