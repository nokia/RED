#
# Copyright 2017 Nokia Solutions and Networks
# Licensed under the Apache License, Version 2.0,
# see license.txt file for details.
#


def start_library_auto_discovering(port, suite_names, variable_mappings, data_source_paths):
    import os
    from robot.run import run
    from TestRunnerAgent import TestRunnerAgent
    from SuiteVisitorImportProxy import SuiteVisitorImportProxy

    current_dir = os.getcwd()

    try:
        if os.path.isdir(data_source_paths[0]):
            os.chdir(data_source_paths[0])

        run(*data_source_paths,
            listener=TestRunnerAgent(port),
            prerunmodifier=SuiteVisitorImportProxy(*suite_names),
            suite=suite_names,
            variable=variable_mappings,
            runemptysuite=True,
            dryrun=True,
            output='NONE',
            report='NONE',
            log='NONE',
            console='NONE')
    finally:
        os.chdir(current_dir)


def start_library_auto_discovering_process(port, suite_names, variable_mappings, data_source_paths, python_paths,
                                           class_paths):
    import os
    import sys
    import platform
    import subprocess

    scripts_location_path = os.path.dirname(os.path.realpath(__file__))
    listener_path = os.path.join(scripts_location_path, 'TestRunnerAgent.py')
    prerunmodifier_path = os.path.join(scripts_location_path, 'SuiteVisitorImportProxy.py')

    command = [sys.executable]

    if 'Jython' in platform.python_implementation():
        path_separator = ';' if _is_windows_platform() else ':'
        command.append('-J-cp')
        command.append(path_separator.join(class_paths))

    command.append('-m')
    command.append('robot.run')
    command.append('--listener')
    command.append(listener_path + ':' + str(port))
    command.append('--prerunmodifier')
    command.append(prerunmodifier_path + ':' + ':'.join(suite_names))
    command.append('--runemptysuite')
    command.append('--dryrun')
    command.append('--output')
    command.append('NONE')
    command.append('--report')
    command.append('NONE')
    command.append('--log')
    command.append('NONE')
    command.append('--console')
    command.append('NONE')

    if python_paths:
        command.append('--pythonpath')
        command.append(':'.join(python_paths + class_paths))

    for suite in suite_names:
        command.append('--suite')
        command.append(suite)

    for variable in variable_mappings:
        command.append('--variable')
        command.append(variable)

    command.extend(data_source_paths)

    if os.path.isdir(data_source_paths[0]):
        subprocess.Popen(command, stdin=subprocess.PIPE, cwd=data_source_paths[0])
    else:
        subprocess.Popen(command, stdin=subprocess.PIPE)


def _is_windows_platform():
    import platform
    if 'Jython' in platform.python_implementation():
        import java.lang.System
        return 'win' in java.lang.System.getProperty('os.name').lower()
    return platform.system() == 'Windows'

if __name__ == '__main__':
    import sys

    args = sys.argv[1:]

    port = args[0]
    args = args[1:]

    suite_names = []
    variable_mappings = []

    if args[0] == '-suitenames':
        suite_names = args[1].split(';')
        args = args[2:]

    if args[0] == '-variables':
        variable_mappings = args[1].split(';')
        args = args[2:]

    data_source_paths = args[0].split(';')
    if len(args) > 1:
        sys.path.extend(args[1].split(';'))

    start_library_auto_discovering(port, suite_names, variable_mappings, data_source_paths)
