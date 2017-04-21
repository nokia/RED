#
# Copyright 2017 Nokia Solutions and Networks
# Licensed under the Apache License, Version 2.0,
# see license.txt file for details.
#


def start_library_auto_discovering(port, timeout, suite_names, data_source_paths):
    import robot.running.namespace
    old_importer = robot.running.namespace.IMPORTER

    try:
        from SuiteVisitorImportProxy import MyIMPORTER
        robot.running.namespace.IMPORTER = MyIMPORTER(robot.running.namespace.IMPORTER, timeout)

        from robot import run_cli
        return run_cli(__create_arguments(port, suite_names, data_source_paths), exit=False)
    finally:
        robot.running.namespace.IMPORTER = old_importer


def __create_arguments(port, suite_names, data_source_paths):
    arguments = ['--listener',
                 'TestRunnerAgent:' + str(port),
                 '--prerunmodifier',
                 'SuiteVisitorImportProxy:',
                 '--runemptysuite',
                 '--dryrun',
                 '--output',
                 'NONE',
                 '--report',
                 'NONE',
                 '--log',
                 'NONE',
                 '--console',
                 'NONE']
    for suite_name in suite_names:
        arguments.append('-s')
        arguments.append(suite_name)
    for data_source_path in data_source_paths:
        arguments.append(data_source_path)
    return arguments


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
