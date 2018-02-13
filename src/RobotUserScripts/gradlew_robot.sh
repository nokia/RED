#!/usr/bin/env bash
#
# Copyright 2018 Nokia Solutions and Networks
# Licensed under the Apache License, Version 2.0,
# see license.txt file for details.
# author: Michal Anglart


first=1
exec=$1
restvar="["

shift
for var in "$@"
do
    if [ $first -eq 1]; then
        restvar="$restvar'$var'"
    else
        restvar="$restvar,'$var'"
    fi
    first=0
done
restvar="$restvar]"

./gradlew runRobot -ProbotExec=$exec -ProbotArguments=$restvar