#
# Copyright 2018 Nokia Solutions and Networks
# Licensed under the Apache License, Version 2.0,
# see license.txt file for details.
#


def start_auto_discovering(port, data_source_path):
    from robot.run import run
    from TestRunnerAgent import TestRunnerAgent
    from SuiteVisitorImportProxy import SuiteVisitorImportProxy

    run(data_source_path,
        listener=TestRunnerAgent(port),
        prerunmodifier=SuiteVisitorImportProxy(True),
        runemptysuite=True,
        dryrun=True,
        output='NONE',
        report='NONE',
        log='NONE',
        console='NONE')


if __name__ == '__main__':
    import sys

    port = sys.argv[1]
    data_source_path = sys.argv[2]

    if len(sys.argv) > 3:
        sys.path.extend(sys.argv[3].split(';'))

    start_auto_discovering(port, data_source_path)