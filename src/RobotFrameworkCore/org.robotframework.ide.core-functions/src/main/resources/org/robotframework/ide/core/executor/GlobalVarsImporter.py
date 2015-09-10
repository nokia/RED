#
# Copyright 2015 Nokia Solutions and Networks
# Licensed under the Apache License, Version 2.0,
# see license.txt file for details.
#

import os
import tempfile

# Global variables copied from robot.variables.__init__.py
global_variables = {
	'${TEMPDIR}': os.path.normpath(tempfile.gettempdir()),
	'${EXECDIR}': os.path.abspath('.'),
	'${/}': os.sep,
	'${:}': os.pathsep,
	'${SPACE}': ' ',
	'${EMPTY}': '',
	'${True}': True,
	'${False}': False,
	'${None}': None,
	'${null}': None,
	'${OUTPUT_DIR}': '',
	'${OUTPUT_FILE}': '',
	'${SUMMARY_FILE}': '',
	'${REPORT_FILE}': '',
	'${LOG_FILE}': '',
	'${DEBUG_FILE}': '',
	'${PREV_TEST_NAME}': '',
	'${PREV_TEST_STATUS}': '',
	'${PREV_TEST_MESSAGE}': '',
	'${CURDIR}': '.',
	'${TEST_NAME}': '',
	'@{TEST_TAGS}': '',
	'${TEST_STATUS}': '',
	'${TEST_MESSAGE}': '',
	'${SUITE_NAME}': '',
	'${SUITE_SOURCE}': '',
	'${SUITE_STATUS}': '',
	'${SUITE_MESSAGE}': ''
}
 
try:
	variables = {}
	try:
		from robot.variables import GLOBAL_VARIABLES
		variables = GLOBAL_VARIABLES
	except ImportError:  # for robot >2.9
		from robot.conf.settings import RobotSettings
		from robot.variables.scopes import GlobalVariables
		variables = GlobalVariables(RobotSettings()).as_dict()
	
	data = {}
	for k in variables.keys():
		if not (k.startswith('${') or k.startswith('@{')):
			key = '${' + k + '}'
		else:
			key = k
		data[key] = str(variables[k])
	
	for k in global_variables:
		if not k in data:
			data[k] = str(global_variables[k])
	
	print str(data)
except Exception, e:
	print str(e)