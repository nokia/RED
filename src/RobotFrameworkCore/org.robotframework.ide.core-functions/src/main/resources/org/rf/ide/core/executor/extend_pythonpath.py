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

def find_module_path(start_path):
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

    return path

def get_module_name_by_path(start_path):
    module_name = start_path
    path_to_module = find_module_path(start_path)
    if (path_to_module != start_path):
        path_to_module = start_path.replace(path_to_module + os.sep, '', 1)
        module_name = path_to_module.replace(os.sep, '.')
        if (os.path.isfile(start_path)):
            module_name = module_name[:-3]

    return module_name

def get_module_combinations(got_from_inspect=list(), module_path=''):
    paths_to_print = list(got_from_inspect)
    module_names = module_path.split('.')[:-1]
    inspect_len = len(got_from_inspect)

    if len(got_from_inspect) == 1 and got_from_inspect[0] == module_path:
        pass
    else:
        if len(module_names) > 0:
            for mod_index in range(len(module_names) - 1, -1, -1):
                pre_index = '.'.join(module_names[mod_index:])
                for get_index in range(0, inspect_len):
                    paths_to_print.append(pre_index + '.' + got_from_inspect[get_index])

    return paths_to_printt

def extend(start_path='.', libImp=''):
    path = find_module_path(start_path)
    path_walk = get_path_to_walk(start_path, path, libImp)

    for k  in pkgutil.walk_packages(path=[path_walk]):
        sys.path.append(k[0].path)
