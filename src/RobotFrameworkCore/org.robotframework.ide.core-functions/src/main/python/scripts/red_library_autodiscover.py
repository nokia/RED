#
# Copyright 2017 Nokia Solutions and Networks
# Licensed under the Apache License, Version 2.0,
# see license.txt file for details.
#


import os
import sys
import platform


def start_auto_discovering(port, data_source_path, support_gevent):
    from robot.run import run
    from TestRunnerAgent import TestRunnerAgent
    from SuiteVisitorImportProxy import SuiteVisitorImportProxy

    run(data_source_path,
        listener=TestRunnerAgent(port),
        prerunmodifier=SuiteVisitorImportProxy(support_gevent=support_gevent),
        runemptysuite=True,
        dryrun=True,
        output='NONE',
        report='NONE',
        log='NONE',
        console='NONE')


def _collect_source_paths(start_path, recursive=True, excluded_paths=[]):
    to_exclude = set([os.path.normpath(os.path.join(start_path, p)) for p in excluded_paths])
    max_depth = start_path.count(os.sep) + 1

    python_paths = list()
    class_paths = list()

    for root, dirs, files in os.walk(start_path):
        for file in files:
            # skip excluded paths
            if __is_excluded_path(root, file, to_exclude):
                continue

            _, extension = os.path.splitext(file)
            if extension == ".py" and not root in python_paths:
                python_paths.append(root)
            elif extension.lower() == ".jar" and 'Jython' in platform.python_implementation():
                class_paths.append(os.path.join(root, file))

        # FIXME: check if such limit is still needed for virtualenv
        if recursive or root.count(os.sep) < max_depth:
            # skip excluded directories
            dirs[:] = [dir for dir in dirs if not __is_excluded_path(root, dir, to_exclude)]
        else:
            # stop traversing
            del dirs[:]

        # ensure fixed traversing order
        dirs.sort()

    return python_paths, class_paths


def __is_excluded_path(root, path, to_exclude):
    return path.startswith('.') or os.path.join(root, path) in to_exclude


def _is_virtualenv():
    return hasattr(sys, 'real_prefix')


if __name__ == '__main__':
    import robot_session_server

    decoded_args = robot_session_server.__decode_unicode_if_needed(sys.argv)

    port = decoded_args[1]
    data_source_path = decoded_args[2]
    project_location_path = decoded_args[3]
    support_gevent = decoded_args[4].lower() == 'true'
    recursive = recursive = not _is_virtualenv() or decoded_args[5].lower() == 'true'
    excluded_paths = []
    additional_paths = []

    if len(decoded_args) > 6:
        if decoded_args[6] == '-exclude':
            excluded_paths = decoded_args[7].split(';')
            if len(decoded_args) > 8:
                additional_paths = decoded_args[8].split(';')
        else:
            additional_paths = decoded_args[6].split(';')

    python_paths, class_paths = _collect_source_paths(project_location_path, recursive, excluded_paths)

    robot_session_server.__extend_paths([project_location_path] + additional_paths + python_paths, class_paths)

    start_auto_discovering(port, data_source_path, support_gevent)
