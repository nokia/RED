# /*
# * Copyright 2017 Nokia Solutions and Networks
# * Licensed under the Apache License, Version 2.0,
# * see license.txt file for details.
# */

# Check RED Robot Editor help for detailed help
# Windows -> Help contents -> RED Robot Editor User Guide -> User guide -> Launching Tests -> Debugging Robot&Python with RED&PyDev

# PyDev needed together with RED on the same Eclipse instance,
# Set PyDev nature to RED project: right click on Project, from PyDev menu: Set as PyDev project
# check paths to pydevd - either from PyDev Eclipse subfolder or by installing with pip
# check paths to robot\run.py - it should be located in local interpreter under Lib\\site-packages\\robot\\run.py
# check ports and ip for PyDev Remote Debug Server
# steps:
# set breakpoints,
# start PyDev remote debug server from Debug perspective (Remote debug server can be running constantly - check PyDev Preferences under PyDev/Debug)
# run Robot Debug configuration with this script

import os
import sys
import subprocess
import inspect


def run_process(command):
    p = subprocess.Popen(command, stdout=subprocess.PIPE)

    while True:
        nextline = p.stdout.readline() if sys.version_info < (3, 0, 0) else str(p.stdout.readline(), 'utf-8')
        if not nextline and p.poll() is not None:
            break
        sys.stdout.write(nextline)
        sys.stdout.flush()


def find_robot_run_path(custom_path):
    try:
        # try to get path from local Python interpreter
        import robot
        path = os.path.join(os.path.dirname(inspect.getfile(robot)), 'run.py')
    except:
        # if not found use user defined
        path = custom_path

    if not os.path.isfile(path):
        raise RuntimeError('"%s" does not point to an existing file' % (path))

    return path


def find_pydevd_path(custom_path):
    try:
        # try to get path from local Python interpreter
        import pydevd
        path = os.path.join(os.path.dirname(inspect.getfile(pydevd)), 'pydevd.py')
    except:
        # if not found use user defined
        path = custom_path

    if not os.path.isfile(path):
        raise RuntimeError('"%s" does not point to an existing file' % (path))

    return path


def run_local_python_debug(pydevd_ip, pydevd_port, pydevd_path, robot_run_path):
    pydevd_args = ['--multiprocess', '--print-in-debugger-startup', '--vm_type', 'python',
                   '--client', pydevd_ip, '--port', pydevd_port, '--file', find_robot_run_path(robot_run_path)]
    robot_run_args = sys.argv[sys.argv.index('robot.run') + 1:]

    command = [sys.executable, '-u', find_pydevd_path(pydevd_path)] + pydevd_args + robot_run_args

    print('Running command: ' + ' '.join(command))
    run_process(command)


if __name__ == '__main__':

    pydevd_ip = '127.0.0.1'
    pydevd_port = '5678'
    pydevd_path = 'change this to reflect to pydevd.py file, check your local Eclipse/Red with installed PyDev <eclipse_or_RED_with_PyDev>/plugins/org.python.pydev/pysrc/pydevd.py'
    robot_run_path = 'change this to reflect to run.py file, for example C:/Python36/Lib/site-packages/robot/run.py'

    run_local_python_debug(pydevd_ip, pydevd_port, pydevd_path, robot_run_path)
