#
# Copyright 2017 Nokia Solutions and Networks
# Licensed under the Apache License, Version 2.0,
# see license.txt file for details.
#


RED_DRYRUN_PROCESSES = []


def start_library_auto_discovering(port, suite_names, variable_mappings, data_source_paths):
    import sys
    import os
    from robot.run import run
    from TestRunnerAgent import TestRunnerAgent
    from SuiteVisitorImportProxy import SuiteVisitorImportProxy

    # current dir is needed in sys.path
    sys.path.append('.')

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


def start_library_auto_discovering_process(port, suite_names, variable_mappings, data_source_paths, python_paths=[],
                                           class_paths=[]):
    import sys
    import os
    import subprocess

    command = [sys.executable]
    command.append(os.path.join(os.path.dirname(os.path.realpath(__file__)), 'red_library_autodiscover.py'))
    command.append(str(port))
    if suite_names:
        command.append('-suitenames')
        command.append(';'.join(suite_names))
    if variable_mappings:
        command.append('-variables')
        command.append(';'.join(variable_mappings))
    command.append(';'.join(data_source_paths))

    dryrun_env = _create_dryrun_environment(python_paths, class_paths)

    RED_DRYRUN_PROCESSES.append(subprocess.Popen(command, stdin=subprocess.PIPE, env=dryrun_env))


def stop_library_auto_discovering_process():
    for process in RED_DRYRUN_PROCESSES:
        process.kill()
    del RED_DRYRUN_PROCESSES[:]


def _create_dryrun_environment(python_paths, class_paths):
    import os
    import platform

    env = os.environ.copy()

    if python_paths:
        _extend_env_path_variable(env, 'PYTHONPATH', python_paths)

    if class_paths and 'Jython' in platform.python_implementation():
        _extend_env_path_variable(env, 'PYTHONPATH', class_paths)
        _extend_env_path_variable(env, 'CLASSPATH', class_paths)

    return env


def _extend_env_path_variable(env, variable_name, paths):
    path_separator = ';' if _is_windows_platform() else ':'
    if variable_name in env:
        env[variable_name] = env[variable_name] + path_separator + path_separator.join(paths)
    else:
        env[variable_name] = path_separator.join(paths)


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
