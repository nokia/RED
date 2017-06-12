#
# Copyright 2017 Nokia Solutions and Networks
# Licensed under the Apache License, Version 2.0,
# see license.txt file for details.
#


def start_library_auto_discovering(port, timeout, suite_names, data_source_paths):
    import os
    from robot.run import run
    from TestRunnerAgent import TestRunnerAgent
    from SuiteVisitorImportProxy import SuiteVisitorImportProxy

    current_dir = os.getcwd()

    try:
        if os.path.isdir(data_source_paths[0]):
            os.chdir(data_source_paths[0])

        return run(*data_source_paths,
                   listener=TestRunnerAgent(port),
                   prerunmodifier=SuiteVisitorImportProxy(*suite_names, timeout=timeout),
                   suite=suite_names,
                   runemptysuite=True,
                   dryrun=True,
                   output='NONE',
                   report='NONE',
                   log='NONE',
                   console='NONE')
    finally:
        os.chdir(current_dir)


if __name__ == '__main__':
    import sys

    port = sys.argv[1]
    timeout = sys.argv[2]
    suite_names = []
    data_source_paths = []

    if sys.argv[3] == '-suitenames':
        suite_names = sys.argv[4].split(';')
        data_source_paths = sys.argv[5].split(';')
        if len(sys.argv) > 6:
            sys.path.extend(sys.argv[6].split(';'))
    else:
        data_source_paths = sys.argv[3].split(';')
        if len(sys.argv) > 4:
            sys.path.extend(sys.argv[4].split(';'))

    print(start_library_auto_discovering(port, timeout, suite_names, data_source_paths))
