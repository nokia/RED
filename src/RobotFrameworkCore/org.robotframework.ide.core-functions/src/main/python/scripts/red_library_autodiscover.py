#
# Copyright 2017 Nokia Solutions and Networks
# Licensed under the Apache License, Version 2.0,
# see license.txt file for details.
#


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


def _collect_source_paths(start_path, recursive=True):
    import os
    import platform

    max_depth = start_path.count(os.sep) + 1

    python_paths = list()
    class_paths = list()

    for root, dirs, files in os.walk(start_path):
        for file in files:
            _, extension = os.path.splitext(file)
            if extension == ".py" and not root in python_paths:
                python_paths.append(root)
            elif extension == ".jar" and 'Jython' in platform.python_implementation():
                class_paths.append(os.path.join(root, file))

        # FIXME: check if such limit is still needed for virtualenv
        if not recursive and root.count(os.sep) >= max_depth:
            del dirs[:]

    return python_paths, class_paths


def _decode_path_if_needed(path):
    import sys
    return path.decode('utf-8') if sys.version_info < (3, 0, 0) else path


def _is_virtualenv():
    import sys
    return hasattr(sys, 'real_prefix')


if __name__ == '__main__':
    import sys
    import platform

    port = sys.argv[1]
    data_source_path = sys.argv[2]
    project_location_path = sys.argv[3]
    recursiveInVirtualenv = sys.argv[4]

    start_path = _decode_path_if_needed(project_location_path)
    recursive = not _is_virtualenv() or recursiveInVirtualenv
    python_paths, class_paths = _collect_source_paths(start_path, recursive)

    sys.path.extend(python_paths + class_paths)
    if 'Jython' in platform.python_implementation():
        for class_path in class_paths:
            from classpath_updater import ClassPathUpdater
            cp_updater = ClassPathUpdater()
            cp_updater.add_file(class_path)

    start_auto_discovering(port, data_source_path)