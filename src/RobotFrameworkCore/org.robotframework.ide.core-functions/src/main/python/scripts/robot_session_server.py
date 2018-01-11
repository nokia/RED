#
# Copyright 2015 Nokia Solutions and Networks
# Licensed under the Apache License, Version 2.0,
# see license.txt file for details.
#
# Author: Mateusz Marzec
#

import sys
import logging

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


def encode_result_or_exception(func):
    import traceback
    def inner(*args, **kwargs):
        result = {'result': None, 'exception': None}
        try:
            result['result'] = func(*args, **kwargs)
            return result
        except:
            msg = traceback.format_exc()
            result['exception'] = msg
            ERR_LOGGER.exception('')
            return result

    return inner


def logargs(func):
    def inner(*args, **kwargs):
        try:        
            if args == None or len(args) == 0:
                STD_LOGGER.info('calling \'%s\' function, no arguments', func.__name__)
            else:
                STD_LOGGER.info('calling\'%s\' function, supplied arguments:', func.__name__)
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


@logresult
@encode_result_or_exception
@logargs
def check_server_availability():
    pass


@logresult
@encode_result_or_exception
@logargs
def get_modules_search_paths():
    import red_modules
    return red_modules.get_modules_search_paths()


@logresult
@encode_result_or_exception
@logargs
def get_module_path(module_name, python_paths, class_paths):
    def to_call():
        import red_modules
        return red_modules.get_module_path(module_name)

    return __extend_paths(to_call, python_paths, class_paths)


@logresult
@encode_result_or_exception
@logargs
def get_run_module_path():
    import red_modules
    return red_modules.get_run_module_path()


@logresult
@encode_result_or_exception
@logargs
def get_classes_from_module(module_location, python_paths, class_paths):
    def to_call():
        import red_module_classes
        return __cleanup_modules(red_module_classes.get_classes_from_module)(module_location)

    return __extend_paths(to_call, python_paths, class_paths)


@logresult
@encode_result_or_exception
@logargs
def get_variables(path, args):
    import red_variables
    return __cleanup_modules(red_variables.get_variables)(path, args)


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
def get_robot_version():
    return __get_robot_version()


@logresult
@encode_result_or_exception
@logargs
def is_virtualenv():
    import red_virtualenv_check
    return red_virtualenv_check.is_virtualenv()


@logresult
@encode_result_or_exception
@logargs
def start_library_auto_discovering(port, data_source_path, python_paths, class_paths):
    import red_library_autodiscover
    red_library_autodiscover.start_library_auto_discovering_process(port,
                                                                    __encode_unicode_if_needed(data_source_path),
                                                                    __encode_unicode_if_needed(python_paths),
                                                                    __encode_unicode_if_needed(class_paths))


@logresult
@encode_result_or_exception
@logargs
def stop_library_auto_discovering():
    import red_library_autodiscover
    red_library_autodiscover.stop_library_auto_discovering_process()


@logresult
@encode_result_or_exception
@logargs
def run_rf_lint(host, port, filepath, additional_arguments):
    import subprocess
    import os

    try:
        import rflint
        import rflint_integration
    except Exception as e:
        raise RuntimeError('There is no rflint module installed for ' + sys.executable + ' interpreter')

    command = [sys.executable]
    command.append(os.path.join(os.path.dirname(os.path.realpath(__file__)), 'rflint_integration.py'))
    command.append(host)
    command.append(str(port))
    command.extend(additional_arguments)
    command.append('-r')
    command.append(__encode_unicode_if_needed(filepath))

    subprocess.Popen(command, stdin=subprocess.PIPE)


@logresult
@encode_result_or_exception
@logargs
def create_libdoc(libname, python_paths, class_paths):
    def to_call():
        import red_libraries
        return __cleanup_modules(red_libraries.create_libdoc)(libname)

    return __extend_paths(to_call, python_paths, class_paths)


def __get_robot_version():
    try:
        import robot
    except ImportError:
        return None
    from robot import version
    return 'Robot Framework ' + version.get_full_version()


def __encode_unicode_if_needed(s):
    if sys.version_info < (3, 0, 0) and isinstance(s, unicode):
        return s.encode('utf-8')
    elif sys.version_info < (3, 0, 0) and isinstance(s, list):
        return [__encode_unicode_if_needed(elem) for elem in s]
    else:
        return s


# decorator which cleans up all the modules that were loaded
# during decorated call
def __cleanup_modules(to_call):
    import sys

    def inner(*args, **kwargs):
        old_modules = set(sys.modules.keys())
        try:
            return to_call(*args, **kwargs)
        except:
            raise
        finally:
            current_modules = set(sys.modules.keys())
            builtin_modules = set(sys.builtin_module_names)

            # some modules should not be removed because it causes rpc server problems
            to_remove = [m for m in current_modules - old_modules - builtin_modules if
                         not m.startswith('robot.') and not m.startswith('encodings.')]
            for m in to_remove:
                del (sys.modules[m])
                del (m)

    return inner


def __extend_paths(to_call, python_paths, class_paths):
    import sys
    from robot import pythonpathsetter

    old_sys_path = list(sys.path)

    __extend_classpath(class_paths)

    for path in python_paths + class_paths:
        pythonpathsetter.add_path(path)

    try:
        return to_call()
    except:
        raise
    finally:
        sys.path = old_sys_path


def __extend_classpath(class_paths):
    import platform

    if 'Jython' in platform.python_implementation():
        for class_path in class_paths:
            from classpath_updater import ClassPathUpdater
            cp_updater = ClassPathUpdater()
            cp_updater.add_file(class_path)


def __shutdown_server_when_parent_process_becomes_unavailable(server):
    import sys

    # this causes the function to block on readline() call; parent process which
    # started this script shouldn't write anything to the input, so this function will
    # be blocked until parent process will be closed/killed; this will cause readline()
    # to read EOF and hence proceed to server.shutdown() which will terminate whole script
    sys.stdin.readline()
    server.shutdown()


if __name__ == '__main__':
    from threading import Thread
    import socket

    socket.setdefaulttimeout(10)

    try:
        from xmlrpc.server import SimpleXMLRPCServer
    except ImportError:
        from SimpleXMLRPCServer import SimpleXMLRPCServer

    IP = '127.0.0.1'
    PORT = int(sys.argv[1])

    server = SimpleXMLRPCServer((IP, PORT), allow_none=True)
    server.register_function(get_modules_search_paths, 'getModulesSearchPaths')
    server.register_function(get_module_path, 'getModulePath')
    server.register_function(get_run_module_path, 'getRunModulePath')
    server.register_function(get_classes_from_module, 'getClassesFromModule')
    server.register_function(get_variables, 'getVariables')
    server.register_function(get_global_variables, 'getGlobalVariables')
    server.register_function(get_standard_libraries_names, 'getStandardLibrariesNames')
    server.register_function(get_standard_library_path, 'getStandardLibraryPath')
    server.register_function(get_robot_version, 'getRobotVersion')
    server.register_function(is_virtualenv, 'isVirtualenv')
    server.register_function(start_library_auto_discovering, 'startLibraryAutoDiscovering')
    server.register_function(stop_library_auto_discovering, 'stopLibraryAutoDiscovering')
    server.register_function(run_rf_lint, "runRfLint")
    server.register_function(create_libdoc, 'createLibdoc')
    server.register_function(check_server_availability, 'checkServerAvailability')

    red_checking_thread = Thread(target=__shutdown_server_when_parent_process_becomes_unavailable, args=(server,))
    red_checking_thread.setDaemon(True)
    red_checking_thread.start()

    robot_ver = __get_robot_version()
    print('# RED session server started @' + str(PORT))
    print('# python version: ' + sys.version)
    print('# robot version: ' + (robot_ver if robot_ver else "<no robot installed>"))
    print('# script path: ' + __file__)
    print('\n')

    server.serve_forever()
