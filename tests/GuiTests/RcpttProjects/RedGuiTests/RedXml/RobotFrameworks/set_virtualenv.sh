#!/usr/bin/env bash
virtualenv $1/ve
source $1/ve/bin/activate
pip list
pip install robotframework
deactivate
