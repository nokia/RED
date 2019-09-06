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

    vars_from_file = _get_variables_from_file(path, arguments)

    filtered_vars = {}
    for k, v in vars_from_file.items():
        try:
            # we filter out modules and functions so that they will not be available
            # in assistant as well as not visible for validator
            if not inspect.ismodule(v) and not inspect.isfunction(v):
                filtered_vars[k] = _escape_unicode(v)
        except:
            filtered_vars[k] = 'None'
    return filtered_vars


def _get_variables_from_file(path, arguments):
    import robot

    variables = robot.variables.Variables()
    variables.set_from_file(path, arguments)

    return variables.store.data


def _escape_unicode(data):
    import sys
    from robot.utils.dotdict import DotDict

    if sys.version_info < (3, 0, 0) and isinstance(data, unicode):
        import unicodedata
        return unicodedata.normalize('NFKD', data).encode('ascii', 'ignore')  # for XML-RPC problems with unicode characters
    elif sys.version_info >= (3, 0, 0) and isinstance(data, str):
        escaped_data = data.encode('unicode_escape')
        if isinstance(escaped_data, bytes):
            escaped_data = escaped_data.decode()
        return escaped_data
    elif sys.version_info < (3, 0, 0) and isinstance(data, basestring):
        return data.encode('unicode_escape')
    elif sys.version_info < (3, 0, 0) and isinstance(data, long):  # for OverflowError in XML-RPC
        return str(data)
    elif isinstance(data, int) and (data < -(2 ** 31) or data > (2 ** 31) - 1):
        return str(data)
    elif isinstance(data, DotDict):
        return dict((_escape_unicode(k), _escape_unicode(v)) for k, v in data.items())
    elif isinstance(data, dict):
        data_result = {}
        for key, val in data.items():
            if isinstance(key, tuple):  # for XML-RPC problems with TypeError (dictionary key must be string)
                return 'None'
            data_result[_escape_unicode(str(key))] = _escape_unicode(val)
        return data_result
    elif isinstance(data, list):
        return list(_escape_unicode(item) for item in data)
    elif isinstance(data, tuple):
        return tuple(_escape_unicode(item) for item in data)
    elif data is None:
        return _escape_unicode('None')
    else:
        return _escape_unicode(str(data))
