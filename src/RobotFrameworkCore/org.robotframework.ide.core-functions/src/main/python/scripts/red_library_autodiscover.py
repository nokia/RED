#
# Copyright 2017 Nokia Solutions and Networks
# Licensed under the Apache License, Version 2.0,
# see license.txt file for details.
#


RED_DRYRUN_PROCESSES = []


def start_library_auto_discovering(port, data_source_path):
    from robot.run import run
    from TestRunnerAgent import TestRunnerAgent
    from SuiteVisitorImportProxy import SuiteVisitorImportProxy

    run(data_source_path,
        listener=TestRunnerAgent(port),
        prerunmodifier=SuiteVisitorImportProxy(),
        runemptysuite=True,
        dryrun=True,
        output='NONE',
        report='NONE',
        log='NONE',
        console='NONE')


def start_library_auto_discovering_process(port, data_source_path, python_paths=[], class_paths=[]):
    import sys
    import os
    import subprocess

    command = [sys.executable]
    command.append(os.path.join(os.path.dirname(os.path.realpath(__file__)), 'red_library_autodiscover.py'))
    command.append(str(port))
    command.append(data_source_path)

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
        env['RED_DRYRUN_PYTHONPATH'] = ';'.join(python_paths)
    if class_paths and 'Jython' in platform.python_implementation():
        env['RED_DRYRUN_CLASSPATH'] = ';'.join(class_paths)
    return env


def _extend_paths_from_dryrun_environment():
    import sys
    import os
    import platform
    if ('RED_DRYRUN_PYTHONPATH' in os.environ):
        sys.path.extend(os.environ['RED_DRYRUN_PYTHONPATH'].split(';'))
    if ('RED_DRYRUN_CLASSPATH' in os.environ):
        sys.path.extend(os.environ['RED_DRYRUN_CLASSPATH'].split(';'))
        if 'Jython' in platform.python_implementation():
            for class_path in os.environ['RED_DRYRUN_CLASSPATH'].split(';'):
                from classpath_updater import ClassPathUpdater
                cp_updater = ClassPathUpdater()
                cp_updater.add_file(class_path)


if __name__ == '__main__':
    import sys

    port = sys.argv[1]
    data_source_path = sys.argv[2]

    if len(sys.argv) > 3:
        sys.path.extend(sys.argv[3].split(';'))
    else:
        _extend_paths_from_dryrun_environment()

    start_library_auto_discovering(port, data_source_path)