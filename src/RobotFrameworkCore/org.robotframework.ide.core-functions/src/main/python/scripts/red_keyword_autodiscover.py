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


def __decode_unicode_if_needed(arg):
    if sys.version_info < (3, 0, 0) and isinstance(arg, str):
        return arg.decode('utf-8')
    elif sys.version_info < (3, 0, 0) and isinstance(arg, list):
        return [__decode_unicode_if_needed(elem) for elem in arg]
    else:
        return arg


if __name__ == '__main__':
    import sys

    port = sys.argv[1]
    data_source_path = sys.argv[2]
    additional_paths = sys.argv[3].split(';') if len(sys.argv) > 3 else []

    sys.path.extend(__decode_unicode_if_needed(additional_paths))

    start_auto_discovering(port, __decode_unicode_if_needed(data_source_path))
