::
:: Copyright 2018 Nokia Solutions and Networks
:: Licensed under the Apache License, Version 2.0,
:: see license.txt file for details.
:: author: Michal Anglart

@echo off
set FIRST="true"
set EXEC=%1
set RF_ARGS=[
shift

:loop1
if "%1"=="" goto after_loop
if %FIRST%=="true" (
    set RF_ARGS=%RF_ARGS%'%1'
) else (
    set RF_ARGS=%RF_ARGS%, '%1'
)
shift
set FIRST="false"
goto loop1

:after_loop
set RF_ARGS=%RF_ARGS:\=/%]