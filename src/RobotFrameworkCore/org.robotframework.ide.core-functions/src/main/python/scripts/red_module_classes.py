#
# Copyright 2017 Nokia Solutions and Networks
# Licensed under the Apache License, Version 2.0,
# see license.txt file for details.
#


import os
import importlib
import pkgutil
import inspect

from robot import pythonpathsetter


def get_classes_from_module(module_location):
    module_directory = os.path.dirname(module_location)
    module_name = _find_module_name_by_path(module_location)
    class_names = list()

    if module_location.endswith('__init__.py'):
        pythonpathsetter.add_path(os.path.dirname(module_directory))
        class_names.extend(_try_to_find_names_in_module(module_name))
        class_names.extend(_try_to_find_names_in_path([module_directory], _find_names_in_module))
        class_names = [_adjust_class_name(module_name, class_name) for class_name in class_names]

    elif module_location.endswith('.py'):
        pythonpathsetter.add_path(module_directory)
        class_names.extend(_try_to_find_names_in_module(module_name))

    elif module_location.endswith(".zip") or module_location.endswith(".jar"):
        pythonpathsetter.add_path(module_location)
        class_names.extend(_try_to_find_names_in_path([module_location], _find_names_in_archive_module))

    else:
        raise Exception('Unrecognized library path: ' + module_location)

    class_names.extend(_find_missing_names(class_names, module_name))
    return sorted(set(class_names))

def _adjust_class_name(module_name, class_name):
    module_name_base = module_name.rsplit('.', 1)[-1]
    if class_name.startswith(module_name) or class_name.startswith(module_name_base):
        return class_name
    else:
        return module_name_base + '.' + class_name

def _find_missing_names(names, module_name):
    result = list()

    if names != [module_name]:
        module_names = module_name.split('.')[:-1]
        if len(module_names) > 0:
            for mod_index in range(len(module_names) - 1, -1, -1):
                pre_index = '.'.join(module_names[mod_index:])
                for get_index in range(0, len(names)):
                    if not names[get_index].startswith(pre_index) and not pre_index + '.' + names[get_index] in result:
                        result.append(pre_index + '.' + names[get_index])

    return result


def _try_to_find_names_in_module(module_name):
    try:
        module = importlib.import_module(module_name)
        return _find_names_in_module(module, module_name)
    except:
        module_name_base = module_name.rsplit('.', 1)[-1]
        if module_name == module_name_base:
            raise
        module = importlib.import_module(module_name_base)
        return _find_names_in_module(module, module_name_base)


def _find_module_path(start_path):
    current_path = os.path.abspath(start_path)
    while True:
        if os.path.isfile(current_path):
            current_path = os.path.dirname(current_path)
        elif os.path.isdir(current_path):
            import sys
            if os.path.exists(current_path + os.sep + '__init__.py') or os.path.basename(current_path) in sys.modules:
                current_path = os.path.normpath(os.path.join(current_path, os.path.pardir))
            else:
                return current_path
        else:
            break

    return os.path.abspath(start_path.split(os.sep)[0])


def _find_module_name_by_path(start_path):
    result = start_path
    path_to_module = _find_module_path(start_path)
    if path_to_module != start_path:
        path_to_replace = path_to_module + os.sep
        if path_to_module.endswith(os.sep):
            path_to_replace = path_to_module
        path_to_module = start_path.replace(path_to_replace, '', 1)
        result = path_to_module.replace(os.sep, '.')
        if os.path.isfile(start_path):
            if start_path.endswith('__init__.py'):
                result = result[:-12]
            else:
                _, start_path_extension = os.path.splitext(start_path)
                result = result[:-len(start_path_extension)]

    return result


def _try_to_find_names_in_path(path, names_finder):
    result = list()
    for loader, name, _ in pkgutil.walk_packages(path):
        try:
            module = loader.find_module(name).load_module(name)
            result.extend(names_finder(module, name))
        except:
            pass  # some modules can't be loaded  separately

    return result


def _find_names_in_module(module, name):
    result = list()
    result.append(module.__name__)
    for _, obj in inspect.getmembers(module):
        if inspect.isclass(obj) and obj.__module__.startswith(name):
            result.append(obj.__module__ + "." + obj.__name__)

    return result


def _find_names_in_archive_module(module, name):
    result = list()
    for n, obj in inspect.getmembers(module):
        if inspect.isfunction(obj):
            result.append(obj.__module__)
        if inspect.isclass(obj) and obj.__module__.startswith(name):
            if obj.__module__ != obj.__name__:
                result.append(obj.__module__ + "." + obj.__name__)
            else:
                result.append(obj.__module__)

    return result
