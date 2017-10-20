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
        '${EXECDIR}': '',
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
        '${TEST_DOCUMENTATION}': '',
        '@{TEST_TAGS}': [],
        '${TEST_STATUS}': '',
        '${TEST_MESSAGE}': '',
        '${SUITE_NAME}': '',
        '${SUITE_SOURCE}': '',
        '${SUITE_STATUS}': '',
        '${SUITE_MESSAGE}': '',
        '${SUITE_DOCUMENTATION}': '',
        '${KEYWORD_MESSAGE}': '',
        '${KEYWORD_STATUS}': ''
    }

    glob_variables = {}
    try:
        from robot.variables import GLOBAL_VARIABLES

        glob_variables = GLOBAL_VARIABLES
    except ImportError:  # for robot >2.9
        global_variables['&{EMPTY}'] = {}
        global_variables['&{SUITE_METADATA}'] = {}
        from robot.conf.settings import RobotSettings
        from robot.variables.scopes import GlobalVariables

        glob_variables = GlobalVariables(RobotSettings()).as_dict()
        glob_variables['${OUTPUT_DIR}'] = ''
        glob_variables['${EXECDIR}'] = ''
        glob_variables['${OUTPUT_FILE}'] = ''
        glob_variables['${REPORT_FILE}'] = ''
        glob_variables['${LOG_FILE}'] = ''

    data = dict((_wrap_variable_if_needed(key), value) for key, value in glob_variables.items())

    for k in global_variables:
        if not k in data:
            data[k] = global_variables[k]
    return data


def _wrap_variable_if_needed(varname):
    if varname.startswith('${') or varname.startswith('@{') or varname.startswith('&{'):
        return varname
    else:
        return '${' + varname + '}'

def get_variables(path, arguments):
    import inspect
    try:
        from robot.utils.dotdict import DotDict
    except:  # for robot <2.9
        class DotDict:
            pass

    vars_from_file = _get_variables_from_file(path, arguments)

    filtered_vars = {}
    for k, v in vars_from_file.items():
        try:
            if isinstance(v, DotDict):
                filtered_vars[k] = _extract_dot_dict(v)
            elif not inspect.ismodule(v) and not inspect.isfunction(v) and not inspect.isclass(v):
                filtered_vars[k] = _escape_unicode(v)
            #we do not support modules, functions or classes as variables
            #else:
            #    filtered_vars[k] = _escape_unicode(str(v))
        except Exception as e:
            filtered_vars[k] = 'None'
    return filtered_vars


def _get_variables_from_file(path, arguments):
    import robot

    variables = robot.variables.Variables()
    variables.set_from_file(path, arguments)
    try:
        return variables.data
    except AttributeError:  # for robot >2.9
        return variables.store.data._data


def _extract_dot_dict(dict):
    return dict((_escape_unicode(k), _escape_unicode(v)) for k, v in dict.items())


def _escape_unicode(data):
    from copy import copy
    # basestring and long is not defined in python3
    import sys
    py_version = sys.version_info
    if py_version < (3,0,0) and isinstance(data, unicode):
        import unicodedata
        return unicodedata.normalize('NFKD', data).encode('ascii','ignore') # for XML-RPC problems with unicode characters
    elif py_version >= (3,0,0) and isinstance(data, str):
        escaped_data = data.encode('unicode_escape')
        if isinstance(escaped_data, bytes):
            escaped_data = escaped_data.decode()
        return escaped_data
    elif py_version < (3,0,0) and isinstance(data, basestring):
        return data.encode('unicode_escape')
    elif py_version < (3,0,0) and isinstance(data, long):  # for OverflowError in XML-RPC
        return str(data)
    elif isinstance(data, int) and (data < -(2**31) or data > (2 ** 31) -1):
        return str(data)
    elif isinstance(data, dict):
        data_result = {}
        for key, val in data.items():
            if isinstance(key, tuple):
                return 'None'
            data_result[_escape_unicode(str(key))] = _escape_unicode(val)
        return data_result
    elif isinstance(data, list):
        data_result = copy(data)
        for index, item in enumerate(data_result):
            data_result[index] = _escape_unicode(item)
        return data_result
    elif isinstance(data, tuple):   
        tuple_data = ()
        for item in data:
            tuple_data = tuple_data + tuple(_escape_unicode(item)) 
        return tuple_data
    elif data is None:
      return _escape_unicode('None')
    else:
      return _escape_unicode(str(data))


if __name__ == '__main__':
    import json
    import sys
    if sys.argv[1] == '-global':
        print(json.dumps(get_global_variables()))
    elif sys.argv[1] == '-variables':
        path = sys.argv[2]
        args = list(sys.argv[3:])
        print(json.dumps(get_variables(path, args)))
