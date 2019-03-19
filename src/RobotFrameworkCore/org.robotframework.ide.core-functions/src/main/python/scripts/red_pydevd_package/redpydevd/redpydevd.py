# 
# Copyright 2019 Nokia Solutions and Networks
# Licensed under the Apache License, Version 2.0,
# see license.txt file for details.
#
import os
import sys
import subprocess
import inspect

__version_info__ = (1, 0, 0)
__version__ = '.'.join(map(str, __version_info__))

def run_process(command):
    p = subprocess.Popen(command, stdout=subprocess.PIPE)

    while True:
        nextline = p.stdout.readline() if sys.version_info < (3, 0, 0) else str(p.stdout.readline(), 'utf-8')
        if not nextline and p.poll() is not None:
            break
        sys.stdout.write(nextline)
        sys.stdout.flush()


def find_robot_run_path():
    try:
        # try to get path from local Python interpreter
        import robot
        path = os.path.join(os.path.dirname(inspect.getfile(robot)), 'run.py')
    except:
        raise RuntimeError('Unable to find robot.run module')

    if not os.path.isfile(path):
        raise RuntimeError('"%s" does not point to an existing file' % (path))
    return path


def run_local_python_debug(args):
    runner_args = args[:args.index('robot.run')]
    robot_args = args[args.index('robot.run') + 1:]

    client_index = runner_args.index('--client') if '--client' in runner_args else -1
    client_ip = runner_args[client_index + 1] if client_index >= 0 else '127.0.0.1'

    port_index = runner_args.index('--port') if '--port' in runner_args else -1
    client_port = runner_args[port_index + 1] if port_index >= 0 else '5678'

    pydevd_index = runner_args.index('--pydevd') if '--pydevd' in runner_args else -1

    pydevd_args = ['--multiprocess', '--print-in-debugger-startup', '--vm_type', 'python',
                   '--client', client_ip, '--port', client_port, '--file', find_robot_run_path()]

    command = [sys.executable, '-u']
    if pydevd_index >= 0:
        command.append(runner_args[pydevd_index + 1])
    else:
        command.append('-m')
        command.append('pydevd')
    command.extend(pydevd_args)
    command.extend(robot_args)

    print('Running command: ' + ' '.join(command))
    run_process(command)


def run_cli():
    run_local_python_debug(sys.argv)


if __name__ == '__main__':
    run_cli()
