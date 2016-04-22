#
# Copyright 2015 Nokia Solutions and Networks
# Licensed under the Apache License, Version 2.0,
# see license.txt file for details.
#

import sys
import platform
import os
import os.path
import inspect
import pkgutil
import imp
import extend_pythonpath
'''
This script prints all the classes contained in given module and submodules rooted in given path. 
In case of package modules the path has to point to __init__.py file of this module.

Call the script with arguments:
   interpreter module_classes_printer.py <module_path> [-pythonpath p1;p2;..;pk] [-modulename name]

'''
original_path = sys.argv[1]
module_name_from_import = ''

i = 2
while i < len(sys.argv):
    if sys.argv[i] == '-pythonpath':
        sys.path.extend(sys.argv[i + 1].split(';'))
        i += 2
    elif sys.argv[i] == '-modulename':
        module_name_from_import = sys.argv[i + 1]
        i += 2
    else:
        i += 1    

if original_path.startswith('"') and original_path.endswith('"'):
    original_path = original_path[1:-1]

modules = []
parent = os.path.dirname(original_path)
path = parent
if original_path.endswith('__init__.py'):
    module_name = os.path.basename(path)
    parent = os.path.dirname(path)
    sys.path.append(parent)

    module_file, module_path, module_desc = imp.find_module(module_name)
    root_module = imp.load_module(module_name, module_file, module_path, module_desc)

    modules = [root_module]
    for loader, name, _ in pkgutil.walk_packages([path]):
        modules.append(loader.find_module(name).load_module(name))

elif original_path.endswith('.py'):
    module_name = os.path.basename(original_path)[:-3]
    sys.path.append(path)
    try:
        module_file, module_path, module_desc = imp.find_module(module_name)
        root_module = imp.load_module(module_name, module_file, module_path, module_desc)
    except Exception as e:
        if module_name_from_import == '':
            module_name_from_import = os.path.basename(original_path)[:-3]
        import importlib
        try:
            extend_pythonpath.extend(original_path, module_name_from_import)
            root_module = importlib.import_module(module_name_from_import)
        except Exception as e:
            tempModuleName = extend_pythonpath.get_module_name_by_path(original_path)
            extend_pythonpath.extend(original_path, tempModuleName)
            root_module = importlib.import_module(tempModuleName)

    modules = [root_module]

elif original_path.endswith(".zip") or original_path.endswith(".jar"):
    toRemove = False
    if not original_path in sys.path:
        sys.path.append(original_path)
        toRemove = True
    for loader, name, _ in pkgutil.walk_packages([original_path]):
        module = loader.load_module(name)
        map(__import__, [name])
        found = {}
        for n, obj in inspect.getmembers(module):
            if inspect.isfunction(obj):
                found[obj.__module__] = module
            if inspect.isclass(obj) and obj.__module__.startswith(name):
                if (obj.__module__ != obj.__name__):
                    found[obj.__module__ + "." + obj.__name__] = module
                else:
                    found[obj.__module__] = module
        for v in found.keys():
            print(v)
    if toRemove:
        sys.path.remove(original_path)

else:
    raise Exception('Unrecognized library path: ' + original_path)

to_print = list()

for module in modules:
    to_print.append(module.__name__)
    for n, obj in inspect.getmembers(module):
        if inspect.isclass(obj) and obj.__module__.startswith(module_name):
            to_print.append(obj.__module__ + '.' + obj.__name__)

if original_path.endswith('.py'):
    to_print = extend_pythonpath.get_module_combinations(to_print, extend_pythonpath.get_module_name_by_path(original_path))

if len(to_print) > 0:
    print('\n'.join(to_print))