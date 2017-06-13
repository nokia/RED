#
# Copyright 2017 Nokia Solutions and Networks
# Licensed under the Apache License, Version 2.0,
# see license.txt file for details.
#


def start_library_auto_discovering(port, timeout, suite_names, variable_mappings, data_source_paths):
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
                   variable=variable_mappings,
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

    args = sys.argv[1:]

    port = args[0]
    timeout = args[1]
    args = args[2:]

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

    print(start_library_auto_discovering(port, timeout, suite_names, variable_mappings, data_source_paths))
