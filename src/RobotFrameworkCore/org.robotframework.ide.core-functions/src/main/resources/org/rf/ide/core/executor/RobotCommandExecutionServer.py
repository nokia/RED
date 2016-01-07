#
# Copyright 2015 Nokia Solutions and Networks
# Licensed under the Apache License, Version 2.0,
# see license.txt file for details.
#
# Author: Mateusz Marzec
#
try:
    from xmlrpc.server import SimpleXMLRPCServer
except ImportError:
    from SimpleXMLRPCServer import SimpleXMLRPCServer

import sys
import os
import tempfile
import importlib


def checkServerAvailability():
    pass


def getModulesSearchPaths():
    import robot  # Robot will add some paths to PYTHONPATH

    return sys.path


def getModulePath(moduleName):
    import imp

    _, path, _ = imp.find_module(moduleName)
    return path


def getVariables(dir, args):
    import robot
    try:
        from robot.utils.dotdict import DotDict
    except:  # for robot <2.9
        class DotDict:
            pass
        pass
    import inspect

    vars = robot.variables.Variables()
    try:
        vars.set_from_file(dir, args)
    except:
        pass
     
    varsFromFile = {}
    try:
        varsFromFile = vars.data
    except AttributeError:  # for robot >2.9
        varsFromFile = vars.store.data._data
        
    filteredVars = {}
    for k, v in varsFromFile.items():
        try:
            if isinstance(v, DotDict):
                filteredVars[k] = extractDotDict(v)
            elif not inspect.ismodule(v) and not inspect.isfunction(v) and not inspect.isclass(v):
                filteredVars[k] = escape_unicode(v)
        except Exception as e:
            filteredVars[k] = 'None'
           
    return filteredVars

def extractDotDict(dict):
    return {k : escape_unicode(v) for k, v in dict.items()}

def escape_unicode(data):
    # basestring and long is not defined in python3
    py_version = sys.version_info
    if py_version < (3,0,0) and isinstance(data, unicode):
        import unicodedata
        return unicodedata.normalize('NFKD', data).encode('ascii','ignore') # for XML-RPC problems with unicode characters
    if py_version >= (3,0,0) and isinstance(data, str):
        return data.encode('unicode_escape')
    if py_version < (3,0,0) and isinstance(data, basestring):
        return data.encode('unicode_escape')
    if py_version >= (3,0,0) and isinstance(data, int) and (data < -(2**31) or data > (2 ** 31) -1):
        return str(data)
    if py_version < (3,0,0) and isinstance(data, long):  # for OverflowError in XML-RPC
        return str(data)
    if isinstance(data, dict):
        for key, val in data.items():
            if isinstance(key, tuple):
                return 'None'
            data[key] = escape_unicode(val)
    if isinstance(data, list):
        for index, item in enumerate(data):
         data[index] = escape_unicode(item)
    if isinstance(data, tuple):   
        tupleData = ()
        for item in data:
            tupleData = tupleData + tuple(escape_unicode(item)) 
        return tupleData
    return data

def getGlobalVariables():
    try:

        import robot

        # Global variables copied from robot.variables.__init__.py
        global_variables = {
            '${TEMPDIR}': os.path.normpath(tempfile.gettempdir()),
            '${EXECDIR}': os.path.abspath('.'),
            '${/}': os.sep,
            '${:}': os.pathsep,
            '${SPACE}': ' ',
            '${EMPTY}': '',
            '@{EMPTY}': list(),
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
            '${TEST DOCUMENTATION}': '',
            '@{TEST_TAGS}': list(),
            '${TEST_STATUS}': '',
            '${TEST_MESSAGE}': '',
            '${SUITE_NAME}': '',
            '${SUITE_SOURCE}': '',
            '${SUITE_STATUS}': '',
            '${SUITE_MESSAGE}': ''
        }

        globVariables = {}
        try:
            from robot.variables import GLOBAL_VARIABLES

            globVariables = GLOBAL_VARIABLES
        except ImportError:  # for robot >2.9
            global_variables['&{EMPTY}'] = dict()
            from robot.conf.settings import RobotSettings
            from robot.variables.scopes import GlobalVariables

            globVariables = GlobalVariables(RobotSettings()).as_dict()

        data = {}
        for k in globVariables.keys():
            if not (k.startswith('${') or k.startswith('@{') or k.startswith('&{')):
                key = '${' + k + '}'
            else:
                key = k
            data[key] = globVariables[k]
        for k in global_variables:
            if not k in data:
                data[k] = global_variables[k]
        return data
    except:
        return dict()


def getStandardLibrariesNames():
    try:
        import robot

        try:
            return list(robot.running.namespace.STDLIB_NAMES)
        except:
            # the std libraries set was moved to other place since Robot 2.9.1
            return list(robot.libraries.STDLIBS)
    except:
        return []


def getStandardLibraryPath(libName):
    try:
        module = importlib.import_module("robot.libraries." + libName)
        return module.__file__
    except:
        return None


def getRobotVersion():
    try:
        import robot
        from robot import version as RobotVersion

        return 'Robot Framework ' + RobotVersion.get_full_version()
    except:
        return None


def getRunModulePath():
    try:
        import robot

        return robot.__file__
    except:
        return None


def createLibdoc(resultFilePath, libName, libPath):
    try:
        import robot
        from robot import pythonpathsetter
        from robot import libdoc

        if (libPath != ''):
            pythonpathsetter.add_path(libPath)
        robot.libdoc.libdoc(libName, resultFilePath, format='XML')
        if (libPath != ''):
            pythonpathsetter.remove_path(libPath)
        return True
    except:
        return False


def __shutdown_server_when_parent_process_becomes_unavailable(server):
    import sys

    # this causes the function to block on readline() call; parent process which 
    # started this script shouldn't write anything to the input, so this function will
    # be blocked until parent process will be closed/killed; this will cause readline()
    # to read EOF and hence proceed to server.shutdown() which will terminate whole script
    sys.stdin.readline()
    server.shutdown()


if __name__ == "__main__":
    IP = '127.0.0.1'
    PORT = int(sys.argv[1])

    server = SimpleXMLRPCServer((IP, PORT), allow_none=True)
    server.register_function(getModulesSearchPaths, "getModulesSearchPaths")
    server.register_function(getModulePath, "getModulePath")
    server.register_function(getVariables, "getVariables")
    server.register_function(getGlobalVariables, "getGlobalVariables")
    server.register_function(getStandardLibrariesNames, "getStandardLibrariesNames")
    server.register_function(getStandardLibraryPath, "getStandardLibraryPath")
    server.register_function(getRobotVersion, "getRobotVersion")
    server.register_function(getRunModulePath, "getRunModulePath")
    server.register_function(createLibdoc, "createLibdoc")
    server.register_function(checkServerAvailability, "checkServerAvailability")

    from threading import Thread

    redCheckingThread = Thread(target=__shutdown_server_when_parent_process_becomes_unavailable, args={server})
    redCheckingThread.setDaemon(True)
    redCheckingThread.start()

    server.serve_forever()
