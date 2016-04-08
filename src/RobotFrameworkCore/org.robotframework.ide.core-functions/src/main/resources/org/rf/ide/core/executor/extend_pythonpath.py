#
# Copyright 2015 Nokia Solutions and Networks
# Licensed under the Apache License, Version 2.0,
# see license.txt file for details.
#

import sys
import pkgutil
import os

def parent(p):
    return os.path.normpath(os.path.join(p, os.path.pardir))

def get_path_to_walk(start_path, found_path, lib_name=None):
    path = found_path
    for path_sys in sys.path:
        if path_sys == found_path:
            path = start_path
            break

    return path

def extend(start_path='.', libImp=''):
    path = os.path.abspath(start_path.split(os.sep)[0])

    do = True
    current_path = os.path.abspath(start_path)
    while do:
        if os.path.isfile(current_path):
            current_path = os.path.dirname(current_path)
        elif os.path.isdir(current_path):
            if os.path.exists(current_path + os.sep + '__init__.py'):
                current_path = parent(current_path)
            else:
                path = current_path
                do = False
                break

    path_walk = get_path_to_walk(start_path, path, libImp)

    for k  in pkgutil.walk_packages(path=[path_walk]):
        sys.path.append(k[0].path)
