#!/usr/bin/env python
import xmlrunner

__unittest = True

from unittest.main import main, TestProgram, USAGE_AS_MAIN
TestProgram.USAGE = USAGE_AS_MAIN

main(module=None, testRunner=xmlrunner.XMLTestRunner())