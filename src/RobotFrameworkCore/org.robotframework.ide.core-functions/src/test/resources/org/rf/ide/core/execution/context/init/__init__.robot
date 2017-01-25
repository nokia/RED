*** Settings ***
Suite Setup  Init Setup Kw
Suite Teardown  Init Teardown Kw

*** Keywords ***
Init Setup Kw
  Log  init setup
  
Init Teardown Kw
  Log  init teardown