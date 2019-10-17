#
# Copyright 2015 Nokia Solutions and Networks
# Licensed under the Apache License, Version 2.0,
# see license.txt file for details.
#
# Author: Mateusz Marzec
#

import sys
import logging
import re

formatter = logging.Formatter('[%(asctime)s.%(msecs)d] %(message)s', '%H:%M:%S')
std_handler = logging.StreamHandler(sys.stdout)
std_handler.setFormatter(formatter)
STD_LOGGER = logging.getLogger(__name__)
STD_LOGGER.setLevel(logging.INFO)
STD_LOGGER.addHandler(std_handler)

err_handler = logging.StreamHandler(sys.stderr)
err_handler.setFormatter(formatter)
ERR_LOGGER = logging.getLogger(__name__ + '_err')
ERR_LOGGER.setLevel(logging.ERROR)
ERR_LOGGER.addHandler(err_handler)

args_handler = logging.StreamHandler(sys.stdout)
args_handler.setFormatter(logging.Formatter('    > %(message)s'))
STD_ARGS_LOGGER = logging.getLogger(__name__ + '_args')
STD_ARGS_LOGGER.setLevel(logging.INFO)
STD_ARGS_LOGGER.addHandler(args_handler)

RED_DRYRUN_PROCESSES = []

INTERPRETER_PATH = sys.executable


def encode_result_or_exception(func):
    import traceback

    def inner(*args, **kwargs):
        result = {'result': None, 'exception': None}
        try:
            result['result'] = func(*args, **kwargs)
            return result
        except Exception as e:
            formatted_lines = traceback.format_exc().splitlines()
            msg = __cut_unimportant_red_exception(formatted_lines)
            result['exception'] = msg
            ERR_LOGGER.exception('')
            return result

    return inner

def __cut_unimportant_red_exception(lines):
    exception_id = -1
    exception_found = False
    for i, line in list(enumerate(lines)):
        if line.startswith('Exception: ') or re.search('^.+Error: ', line):
            if exception_found:
                exception_id = i
                break
            else:
                exception_found = True
    exception_id = exception_id if exception_id > -1 else 0
    return '\n'.join(lines[exception_id:])

def logargs(func):

    def inner(*args, **kwargs):
        try:
            if args == None or len(args) == 0:
                STD_LOGGER.info('calling \'%s\' function, no arguments', func.__name__)
            else:
                STD_LOGGER.info('calling \'%s\' function, supplied arguments:', func.__name__)
                for arg in args:
                    STD_ARGS_LOGGER.info(arg)
        finally:
            return func(*args, **kwargs)

    return inner


def logresult(func):

    def inner(*args, **kwargs):
        ret = func(*args, **kwargs)
        try:
            if ret['exception']:
                STD_LOGGER.info('call ended with exception, see stderr for details')
            else:
                STD_LOGGER.info('call ended with result:')
                STD_ARGS_LOGGER.info(ret['result'])
        finally:
            return ret

    return inner


# decorator which cleans up all modules that were loaded during decorated call
def cleanup_modules(to_call):

    def inner(*args, **kwargs):
        old_modules = sys.modules.copy()
        try:
            return to_call(*args, **kwargs)
        except:
            raise
        finally:
            current_modules = sys.modules
            builtin_modules = set(sys.builtin_module_names)
            to_preserve_with_submodules = ['robot', 'encodings', 'pkg_resources', 'typing']
            # some modules should not be removed because it causes rpc server problems
            to_remove = [m for m in current_modules if
                         m not in builtin_modules and not __has_to_be_preserved(m, to_preserve_with_submodules) and
                         not (m in old_modules and old_modules[m] is current_modules[m])]

            for m in to_remove:
                del sys.modules[m]
                del m

    return inner


def __has_to_be_preserved(module_name, modules_to_preserve):
    for to_preserve in modules_to_preserve:
        if module_name == to_preserve or module_name.startswith(to_preserve + '.'):
            return True
    return False


# decorator which cleans system path changed during decorated call
def cleanup_sys_path(to_call):

    def inner(*args, **kwargs):
        old_sys_path = list(sys.path)

        try:
            return to_call(*args, **kwargs)
        except:
            raise
        finally:
            sys.path = old_sys_path

    return inner


@logresult
@encode_result_or_exception
@logargs
def check_server_availability(interpreter_path):
    global INTERPRETER_PATH
    if interpreter_path != '':
        INTERPRETER_PATH = interpreter_path


@logresult
@encode_result_or_exception
@logargs
def get_modules_search_paths():
    import red_modules
    return red_modules.get_modules_search_paths()


@logresult
@encode_result_or_exception
@cleanup_sys_path
@logargs
def get_module_path(module_name, python_paths, class_paths):
    import red_modules
    __extend_paths(python_paths, class_paths)
    return red_modules.get_module_path(module_name)


@logresult
@encode_result_or_exception
@cleanup_modules
@cleanup_sys_path
@logargs
def get_classes_from_module(module_location, python_paths, class_paths):
    import red_module_classes
    __extend_paths(python_paths, class_paths)
    return red_module_classes.get_classes_from_module(module_location)


@logresult
@encode_result_or_exception
@cleanup_modules
@cleanup_sys_path
@logargs
def get_variables(path, args, python_paths):
    import os
    cwd = os.getcwd()
    
    try:
        os.chdir(os.path.dirname(path))
        import red_variables
        __extend_paths(python_paths, [])
        return red_variables.get_variables(path, args)
    finally:
        os.chdir(cwd)


@logresult
@encode_result_or_exception
@logargs
def get_global_variables():
    import red_variables
    return red_variables.get_global_variables()


@logresult
@encode_result_or_exception
@logargs
def get_standard_libraries_names():
    import red_libraries
    return red_libraries.get_standard_library_names()


@logresult
@encode_result_or_exception
@logargs
def get_standard_library_path(libname):
    import red_libraries
    return red_libraries.get_standard_library_path(libname)


@logresult
@encode_result_or_exception
@logargs
def get_site_packages_libraries_names():
    import red_libraries
    return red_libraries.get_site_packages_libraries_names()


@logresult
@encode_result_or_exception
@logargs
def get_robot_version():
    return __get_robot_version()


@logresult
@encode_result_or_exception
@logargs
def start_library_auto_discovering(port, data_source_path, project_location_path, support_gevent, recursiveInVirtualenv,
                                   excluded_paths, python_paths, class_paths):
    import subprocess
    import os

    global INTERPRETER_PATH

    command = [INTERPRETER_PATH]
    command.append(os.path.join(os.path.dirname(os.path.realpath(__file__)), 'red_library_autodiscover.py'))
    command.append(str(port))
    command.append(data_source_path)
    command.append(project_location_path)
    command.append(str(support_gevent))
    command.append(str(recursiveInVirtualenv))
    if excluded_paths:
        command.append('-exclude')
        command.append(';'.join(excluded_paths))
    command.append(';'.join(python_paths + class_paths))

    encoded_command = __encode_unicode_if_needed(command)
    RED_DRYRUN_PROCESSES.append(subprocess.Popen(encoded_command, stdin=subprocess.PIPE))


@logresult
@encode_result_or_exception
@logargs
def start_keyword_auto_discovering(port, data_source_path, support_gevent, python_paths, class_paths):
    import subprocess
    import os

    global INTERPRETER_PATH

    command = [INTERPRETER_PATH]
    command.append(os.path.join(os.path.dirname(os.path.realpath(__file__)), 'red_keyword_autodiscover.py'))
    command.append(str(port))
    command.append(data_source_path)
    command.append(str(support_gevent))
    command.append(';'.join(python_paths + class_paths))

    encoded_command = __encode_unicode_if_needed(command)
    RED_DRYRUN_PROCESSES.append(subprocess.Popen(encoded_command, stdin=subprocess.PIPE))


@logresult
@encode_result_or_exception
@logargs
def stop_auto_discovering():
    for process in RED_DRYRUN_PROCESSES:
        process.kill()
    del RED_DRYRUN_PROCESSES[:]

@logresult
@encode_result_or_exception
@cleanup_modules
@logargs
def get_rf_lint_rules(rulesfiles):
    try:
        import rflint
        import rflint_integration
    except Exception as e:
        raise RuntimeError('There is no rflint module installed for ' + INTERPRETER_PATH + ' interpreter')

    return rflint_integration.get_rules(rulesfiles)

@logresult
@encode_result_or_exception
@logargs
def run_rf_lint(host, port, project_location_path, excluded_paths, filepath, additional_arguments):
    global INTERPRETER_PATH

    import subprocess
    import os
    try:
        import rflint
        import rflint_integration
    except Exception as e:
        raise RuntimeError('There is no rflint module installed for ' + INTERPRETER_PATH + ' interpreter')

    command = [INTERPRETER_PATH]
    command.append(os.path.join(os.path.dirname(os.path.realpath(__file__)), 'rflint_integration.py'))
    command.append(host)
    command.append(str(port))
    command.append(project_location_path)
    if excluded_paths:
        command.append('-exclude')
        command.append(';'.join(excluded_paths))
    command.extend(additional_arguments)
    command.append('-r')
    command.append(filepath)

    encoded_command = __encode_unicode_if_needed(command)
    subprocess.Popen(encoded_command, stdin=subprocess.PIPE)


@logresult
@encode_result_or_exception
@logargs
def convert_robot_data_file(original_filepath):
    from robot.tidy import Tidy
    from base64 import b64encode

    converted_content = Tidy(format='robot').file(original_filepath)

    if sys.version_info < (3, 0, 0):
        return b64encode(converted_content.encode('utf-8'))
    else:
        return str(b64encode(bytes(converted_content, 'utf-8')), 'utf-8')


@logresult
@encode_result_or_exception
@cleanup_modules
@cleanup_sys_path
@logargs
def create_libdoc(libname, format, python_paths, class_paths):
    import red_libraries
    __extend_paths(python_paths, class_paths)
    return red_libraries.create_libdoc(libname, format)


@logresult
@encode_result_or_exception
@cleanup_modules
@cleanup_sys_path
@logargs
def create_libdoc_in_separate_process(libname, format, python_paths, class_paths, timeout_duration=30):
    import os
    import subprocess
    import threading
    import time
    try:
        import Queue as queue
    except:
        import queue

    global INTERPRETER_PATH

    command = [INTERPRETER_PATH]
    command.append(os.path.join(os.path.dirname(os.path.realpath(__file__)), 'red_libraries.py'))
    command.append(libname)
    command.append(format)
    command.append(';'.join(python_paths))
    if class_paths:
        command.append(';'.join(class_paths))

    def get_output(process, q_stdout, q_stderr):
        out, err = process.communicate()
        q_stdout.put(out)
        q_stderr.put(err)

    encoded_command = __encode_unicode_if_needed(command)
    process = subprocess.Popen(encoded_command, stdout=subprocess.PIPE, stdin=subprocess.PIPE, stderr=subprocess.PIPE)
    q_stdout = queue.Queue()
    q_stderr = queue.Queue()
    thread = threading.Thread(target=get_output, args=(process, q_stdout, q_stderr))
    thread.daemon = True
    thread.start()

    try:
        out = q_stdout.get(timeout=timeout_duration)
        err = q_stderr.get()

        if out:
            return next(line[8:].rstrip().decode('UTF-8') for line in out.splitlines() if line.startswith(b'Libdoc >'))
        else:
            raise Exception(err.decode('UTF-8', 'replace'))
    except queue.Empty:
        process.kill()
        raise queue.Empty('Libdoc not generated due to timeout')


@logresult
@encode_result_or_exception
@logargs
def create_html_doc(doc, format):
    import red_libraries
    return red_libraries.create_html_doc(doc, format)


def __get_robot_version():
    try:
        import robot
    except ImportError:
        return None
    from robot import version
    return 'Robot Framework ' + version.get_full_version()


def __encode_unicode_if_needed(arg):
    if sys.version_info < (3, 0, 0) and isinstance(arg, unicode):
        return arg.encode('utf-8', 'replace')
    elif sys.version_info < (3, 0, 0) and isinstance(arg, list):
        return [__encode_unicode_if_needed(elem) for elem in arg]
    else:
        return arg


def __decode_unicode_if_needed(arg):
    if sys.version_info < (3, 0, 0) and isinstance(arg, str):
        return arg.decode('utf-8', 'replace')
    elif sys.version_info < (3, 0, 0) and isinstance(arg, list):
        return [__decode_unicode_if_needed(elem) for elem in arg]
    else:
        return arg


def __extend_paths(python_paths, class_paths):
    from robot import pythonpathsetter

    __extend_classpath(class_paths)

    for path in python_paths + class_paths:
        pythonpathsetter.add_path(path)


def __extend_classpath(class_paths):
    import platform

    if 'Jython' in platform.python_implementation():
        for class_path in class_paths:
            from classpath_updater import ClassPathUpdater
            cp_updater = ClassPathUpdater()
            cp_updater.add_file(class_path)


def __get_script_path():
    is_py2 = sys.version_info < (3, 0, 0)
    is_tty = sys.stdout.isatty()

    decoded = __file__.decode(sys.getfilesystemencoding()) if is_py2 else __file__
    encoded = decoded.encode(sys.stdout.encoding if is_tty else 'utf-8')
    return encoded if is_py2 else str(encoded, 'utf-8')


def __create_server(address):
    try:
        from xmlrpc.server import SimpleXMLRPCServer
    except ImportError:
        from SimpleXMLRPCServer import SimpleXMLRPCServer

    server = SimpleXMLRPCServer(address, allow_none=True)

    server.register_function(get_modules_search_paths, 'getModulesSearchPaths')
    server.register_function(get_module_path, 'getModulePath')
    server.register_function(get_classes_from_module, 'getClassesFromModule')
    server.register_function(get_variables, 'getVariables')
    server.register_function(get_global_variables, 'getGlobalVariables')
    server.register_function(get_standard_libraries_names, 'getStandardLibrariesNames')
    server.register_function(get_standard_library_path, 'getStandardLibraryPath')
    server.register_function(get_site_packages_libraries_names, 'getSitePackagesLibrariesNames')
    server.register_function(get_robot_version, 'getRobotVersion')
    server.register_function(start_library_auto_discovering, 'startLibraryAutoDiscovering')
    server.register_function(start_keyword_auto_discovering, 'startKeywordAutoDiscovering')
    server.register_function(stop_auto_discovering, 'stopAutoDiscovering')
    server.register_function(convert_robot_data_file, 'convertRobotDataFile')
    server.register_function(get_rf_lint_rules, 'getRfLintRules')
    server.register_function(run_rf_lint, 'runRfLint')
    server.register_function(create_libdoc, 'createLibdoc')
    server.register_function(create_libdoc_in_separate_process, 'createLibdocInSeparateProcess')
    server.register_function(create_html_doc, 'createHtmlDoc')
    server.register_function(check_server_availability, 'checkServerAvailability')

    return server


def __start_red_checking_thread(server):
    import socket
    socket.setdefaulttimeout(10)

    from threading import Thread
    red_checking_thread = Thread(target=__shutdown_server_when_parent_process_becomes_unavailable, args=(server,))
    red_checking_thread.setDaemon(True)
    red_checking_thread.start()


def __shutdown_server_when_parent_process_becomes_unavailable(server):
    # this causes the function to block on readline() call; parent process which
    # started this script shouldn't write anything to the input, so this function will
    # be blocked until parent process will be closed/killed; this will cause readline()
    # to read EOF and hence proceed to server.shutdown() which will terminate whole script
    sys.stdin.readline()
    server.shutdown()


if __name__ == '__main__':

    IP = '127.0.0.1'
    PORT = int(sys.argv[1])
    ROBOT_VERSION = __get_robot_version()
    SCRIPT_PATH = __get_script_path()

    # server has to be started after retrieving version from robot
    server = __create_server((IP, PORT))

    __start_red_checking_thread(server)

    print('# RED session server started @' + str(PORT))
    print('# python version: ' + sys.version)
    print('# robot version: ' + (ROBOT_VERSION if ROBOT_VERSION else '<no robot installed>'))
    print('# script path: ' + SCRIPT_PATH)
    print('\n')

    server.serve_forever()
