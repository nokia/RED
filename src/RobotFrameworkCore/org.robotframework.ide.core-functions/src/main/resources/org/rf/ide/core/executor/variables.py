#
# Copyright 2016 Nokia Solutions and Networks
# Licensed under the Apache License, Version 2.0,
# see license.txt file for details.
#

def get_global_variables():
    import robot
    import tempfile
    import os
    # Global variables copied from robot.variables.__init__.py
    global_variables = {
        '${TEMPDIR}': os.path.normpath(tempfile.gettempdir()),
        '${EXECDIR}': os.path.abspath('.'),
        '${/}': os.sep,
        '${:}': os.pathsep,
        '${SPACE}': ' ',
        '${EMPTY}': '',
        '@{EMPTY}': [],
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
        '@{TEST_TAGS}': [],
        '${TEST_STATUS}': '',
        '${TEST_MESSAGE}': '',
        '${SUITE_NAME}': '',
        '${SUITE_SOURCE}': '',
        '${SUITE_STATUS}': '',
        '${SUITE_MESSAGE}': ''
    }

    glob_variables = {}
    try:
        from robot.variables import GLOBAL_VARIABLES

        glob_variables = GLOBAL_VARIABLES
    except ImportError:  # for robot >2.9
        global_variables['&{EMPTY}'] = {}
        from robot.conf.settings import RobotSettings
        from robot.variables.scopes import GlobalVariables

        glob_variables = GlobalVariables(RobotSettings()).as_dict()

    data = {_wrap_variable_if_needed(key) : value for key, value in glob_variables.items()}

    for k in global_variables:
        if not k in data:
            data[k] = global_variables[k]
    return data

def _wrap_variable_if_needed(varname):
    if varname.startswith('${') or varname.startswith('@{') or varname.startswith('&{'):
        return varname
    else:
        return '${' + varname + '}'

def get_variables(dir, arguments):
    import robot.variables as rv
    try:
        from robot.utils.dotdict import DotDict
    except:  # for robot <2.9
        class DotDict:
            pass
    import inspect
    
    vars = rv.Variables()
    try:
        vars.set_from_file(dir, arguments)
    except:
        pass
    
    vars_from_file = {}
    try:
        vars_from_file = vars.data
    except AttributeError:  # for robot >2.9
        vars_from_file = vars.store.data._data
    
    filtered_vars = {}
    for k, v in vars_from_file.items():
        try:
            if isinstance(v, DotDict):
                filtered_vars[k] = _extractDotDict(v)
            elif not inspect.ismodule(v) and not inspect.isfunction(v) and not inspect.isclass(v):
                filtered_vars[k] = _escape_unicode(v)
        except Exception as e:
            filtered_vars[k] = 'None'
    return filtered_vars

def _extractDotDict(dict):
    return {k : _escape_unicode(v) for k, v in dict.items()}


def _escape_unicode(data):
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
            data[key] = _escape_unicode(val)
    if isinstance(data, list):
        for index, item in enumerate(data):
         data[index] = _escape_unicode(item)
    if isinstance(data, tuple):   
        tuple_data = ()
        for item in data:
            tuple_data = tuple_data + tuple(_escape_unicode(item)) 
        return tuple_data
    return data
    
if __name__ == '__main__':
    import json
    import sys
    if sys.argv[1] == '-global':
        print(json.dumps(get_global_variables()))
    elif sys.argv[1] == '-variables':
        path = sys.argv[2]
        args = list(sys.argv[3:])
        print(json.dumps(get_variables(path, args)))