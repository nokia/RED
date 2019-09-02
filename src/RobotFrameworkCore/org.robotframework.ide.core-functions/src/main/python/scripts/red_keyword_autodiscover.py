#
# Copyright 2018 Nokia Solutions and Networks
# Licensed under the Apache License, Version 2.0,
# see license.txt file for details.
#


def start_auto_discovering(port, data_source_path, support_gevent):
    from robot.run import run
    from TestRunnerAgent import TestRunnerAgent
    from SuiteVisitorImportProxy import SuiteVisitorImportProxy

    run(data_source_path,
        listener=TestRunnerAgent(port),
        prerunmodifier=SuiteVisitorImportProxy(True, support_gevent),
        runemptysuite=True,
        dryrun=True,
        output='NONE',
        report='NONE',
        log='NONE',
        console='NONE')


if __name__ == '__main__':
    import robot_session_server
    import sys

    decoded_args = robot_session_server.__decode_unicode_if_needed(sys.argv)

    port = decoded_args[1]
    data_source_path = decoded_args[2]
    support_gevent = decoded_args[3].lower() == 'true'
    additional_paths = decoded_args[4].split(';') if len(decoded_args) > 4 else []

    robot_session_server.__extend_paths(additional_paths, [])

    start_auto_discovering(port, data_source_path, support_gevent)
