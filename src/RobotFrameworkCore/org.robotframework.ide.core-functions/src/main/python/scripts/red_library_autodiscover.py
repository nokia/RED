#
# Copyright 2017 Nokia Solutions and Networks
# Licensed under the Apache License, Version 2.0,
# see license.txt file for details.
#


import os
import sys
import platform


def start_auto_discovering(port, data_source_path):
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
            elif extension == ".jar" and 'Jython' in platform.python_implementation():
                class_paths.append(os.path.join(root, file))

        # FIXME: check if such limit is still needed for virtualenv
        if recursive or root.count(os.sep) < max_depth:
            # skip excluded directories
            dirs[:] = [dir for dir in dirs if not __is_excluded_path(root, dir, to_exclude)]
        else:
            # stop traversing
            del dirs[:]

    return python_paths, class_paths


def __is_excluded_path(root, path, to_exclude):
    return path.startswith('.') or os.path.join(root, path) in to_exclude


def __decode_unicode_if_needed(arg):
    if sys.version_info < (3, 0, 0) and isinstance(arg, str):
        return arg.decode('utf-8')
    elif sys.version_info < (3, 0, 0) and isinstance(arg, list):
        return [__decode_unicode_if_needed(elem) for elem in arg]
    else:
        return arg


def _is_virtualenv():
    return hasattr(sys, 'real_prefix')


if __name__ == '__main__':
    port = sys.argv[1]
    data_source_path = __decode_unicode_if_needed(sys.argv[2])
    project_location_path = __decode_unicode_if_needed(sys.argv[3])
    recursive = recursive = not _is_virtualenv() or sys.argv[4]
    excluded_paths = []
    additional_paths = []

    if len(sys.argv) > 5:
        if sys.argv[5] == '-excluded':
            excluded_paths = __decode_unicode_if_needed(sys.argv[6].split(';'))
            if len(sys.argv) > 7:
                additional_paths = __decode_unicode_if_needed(sys.argv[7].split(';'))
        else:
            additional_paths = __decode_unicode_if_needed(sys.argv[5].split(';'))

    python_paths, class_paths = _collect_source_paths(project_location_path, recursive, excluded_paths)

    sys.path.extend([project_location_path] + additional_paths + python_paths + class_paths)
    if 'Jython' in platform.python_implementation():
        for class_path in class_paths:
            from classpath_updater import ClassPathUpdater
            cp_updater = ClassPathUpdater()
            cp_updater.add_file(class_path)

    start_auto_discovering(port, data_source_path)