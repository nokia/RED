#
# Copyright 2015 Nokia Solutions and Networks
# Licensed under the Apache License, Version 2.0,
# see license.txt file for details.
#
# Author: Mateusz Marzec
#

import xmlrpclib
from SimpleXMLRPCServer import SimpleXMLRPCServer

import sys
import os
import tempfile
import importlib

import robot
from robot import version as RobotVersion
from robot import libdoc
from robot import pythonpathsetter

globVariables = {}
try:
    from robot.variables import GLOBAL_VARIABLES
    globVariables = GLOBAL_VARIABLES
except ImportError:  # for robot >2.9
    from robot.conf.settings import RobotSettings
    from robot.variables.scopes import GlobalVariables
    globVariables = GlobalVariables(RobotSettings()).as_dict()

try:
    stdLibNames = robot.running.namespace.STDLIB_NAMES
except:
    # the std libraries set was moved to other place since Robot 2.9.1
    stdLibNames = robot.libraries.STDLIBS

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

def checkServerAvailability():
    return ''

def getVariables(dir, args):
    vars = robot.variables.Variables()
    try:
        exec("vars.set_from_file('"+dir+"',"+str(args)+")")
    except:
        pass
    result = ''
    try:
        result = str(vars.data)
    except AttributeError:  # for robot >2.9
        result = str(vars.store.data)
    return result

def getGlobalVariables():
    try:
        data = {}
        for k in globVariables.keys():
            if not (k.startswith('${') or k.startswith('@{')):
                key = '${' + k + '}'
            else:
                key = k
            data[key] = str(globVariables[k])
        for k in global_variables:
            if not k in data:
                data[k] = str(global_variables[k])
        return str(data)
    except Exception, e:
        return str(e)

def getStandardLibrariesNames():
    try:
        return str(list(stdLibNames))
    except Exception, e:
        return str(e)

def getStandardLibraryPath(libName):
    try:
        module=importlib.import_module("robot.libraries." + libName)
        return module.__file__
    except Exception, e:
        return str(e)

def getRobotVersion():
    try:
        return 'Robot Framework ' + RobotVersion.get_full_version()
    except Exception, e:
        return str(e)

def getRunModulePath():
    try:
        return robot.__file__
    except Exception, e:
        return str(e)

def createLibdoc(resultFilePath, libName, libPath):
    try:
        if(libPath != ''):
            pythonpathsetter.add_path(libPath)
        robot.libdoc.libdoc(libName, resultFilePath, format='XML')
        return 0
    except Exception, e:
        return 1

def close():
    server.server_close()
    return ''

IP = '127.0.0.1'
PORT = int(sys.argv[1])
server = SimpleXMLRPCServer((IP, PORT))
server.register_function(getVariables, "getVariables")
server.register_function(getGlobalVariables, "getGlobalVariables")
server.register_function(getStandardLibrariesNames, "getStandardLibrariesNames")
server.register_function(getStandardLibraryPath, "getStandardLibraryPath")
server.register_function(getRobotVersion, "getRobotVersion")
server.register_function(getRunModulePath, "getRunModulePath")
server.register_function(createLibdoc, "createLibdoc")
server.register_function(checkServerAvailability, "checkServerAvailability")
server.register_function(close, "close")
server.serve_forever()
