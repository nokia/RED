#
# Copyright 2017 Nokia Solutions and Networks
# Licensed under the Apache License, Version 2.0,
# see license.txt file for details.
#


def get_classes_from_module(module_location):
    import os
    from robot import pythonpathsetter

    module_directory = os.path.dirname(module_location)
    module_name = _find_module_name_by_path(module_location)
    class_names = list()

    if module_location.endswith('__init__.py'):
        pythonpathsetter.add_path(os.path.dirname(module_directory))
        module = _import_module(os.path.basename(module_directory), module_name)
        class_names.extend(_find_names_in_module(module, module_name))
        class_names.extend(_find_names_in_path([module_directory], _find_names_in_module))
        class_names = [n if n.startswith(module_name) else module_name + "." + n for n in class_names]

    elif module_location.endswith('.py'):
        pythonpathsetter.add_path(module_directory)
        module = _import_module(os.path.basename(module_location)[:-3], module_name)
        class_names.extend(_find_names_in_module(module, module_name))

    elif module_location.endswith(".zip") or module_location.endswith(".jar"):
        pythonpathsetter.add_path(module_location)
        class_names.extend(_find_names_in_path([module_location], _find_names_in_archive_module))

    else:
        raise Exception('Unrecognized library path: ' + module_location)

    return sorted(set(class_names))


def _import_module(module_name_from_location, module_name):
    import importlib

    try:
        return importlib.import_module(module_name_from_location)
    except:
        return importlib.import_module(module_name)


def _find_module_path(start_path):
    import os

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
    import os

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


def _find_names_in_path(path, names_finder):
    import pkgutil

    result = list()
    for loader, name, _ in pkgutil.walk_packages(path):
        try:
            module = loader.find_module(name).load_module(name)
            result.extend(names_finder(module, name))
        except:
            pass  # some modules can't be loaded  separately

    return result


def _find_names_in_module(module, name):
    import inspect

    result = list()
    result.append(module.__name__)
    for _, obj in inspect.getmembers(module):
        if inspect.isclass(obj) and obj.__module__.startswith(name):
            result.append(obj.__module__ + "." + obj.__name__)

    return result


def _find_names_in_archive_module(module, name):
    import inspect

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


if __name__ == '__main__':
    import sys
    import json

    module_location = sys.argv[1]

    if len(sys.argv) > 2:
        sys.path.extend(sys.argv[2].split(';'))

    print(json.dumps(get_classes_from_module(module_location)))
