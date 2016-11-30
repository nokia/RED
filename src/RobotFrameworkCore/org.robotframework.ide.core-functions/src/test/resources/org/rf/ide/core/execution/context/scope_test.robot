*** Settings ***
Resource  scope_test_res.robot

*** Test Cases ***
Test
  key
  key_from_resource
  scope_test_res.key

*** Keywords ***
key
  Log  internal_test_case_keyword