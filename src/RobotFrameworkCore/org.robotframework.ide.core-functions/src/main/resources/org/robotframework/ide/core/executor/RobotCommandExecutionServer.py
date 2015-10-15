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
    import robot
    return sys.path
    
def getVariables(dir, args):
    import robot
    vars = robot.variables.Variables()
    try:
        exec("vars.set_from_file('"+dir+"',"+str(args)+")")
    except:
        pass
        
    try:
        return vars.data
    except AttributeError:  # for robot >2.9
        return vars.store.data

def getGlobalVariables():
    try:
    
        import robot

        globVariables = {}
        try:
            from robot.variables import GLOBAL_VARIABLES
            globVariables = GLOBAL_VARIABLES
        except ImportError:  # for robot >2.9
            from robot.conf.settings import RobotSettings
            from robot.variables.scopes import GlobalVariables
            globVariables = GlobalVariables(RobotSettings()).as_dict()

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
        data = {}
        for k in globVariables.keys():
            if not (k.startswith('${') or k.startswith('@{')):
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
        module=importlib.import_module("robot.libraries." + libName)
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
        if(libPath != ''):
            pythonpathsetter.add_path(libPath)
        robot.libdoc.libdoc(libName, resultFilePath, format='XML')
        if(libPath != ''):
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
    
    server = SimpleXMLRPCServer((IP, PORT),allow_none=True)
    server.register_function(getModulesSearchPaths, "getModulesSearchPaths")
    server.register_function(getVariables, "getVariables")
    server.register_function(getGlobalVariables, "getGlobalVariables")
    server.register_function(getStandardLibrariesNames, "getStandardLibrariesNames")
    server.register_function(getStandardLibraryPath, "getStandardLibraryPath")
    server.register_function(getRobotVersion, "getRobotVersion")
    server.register_function(getRunModulePath, "getRunModulePath")
    server.register_function(createLibdoc, "createLibdoc")
    server.register_function(checkServerAvailability, "checkServerAvailability")
    
    from threading import Thread
    redCheckingThread = Thread(target = __shutdown_server_when_parent_process_becomes_unavailable, args = {server})
    redCheckingThread.setDaemon(True)
    redCheckingThread.start()
    
    server.serve_forever()