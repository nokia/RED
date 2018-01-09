#
# Copyright 2017 Nokia Solutions and Networks
# Licensed under the Apache License, Version 2.0,
# see license.txt file for details.
#


# FIXME: check if module_name is needed and eventually replace it with boolean
def get_classes_from_module(module_location, module_name=None):
    import os
    from robot import pythonpathsetter

    module_directory = os.path.dirname(module_location)
    module_full_name = _find_module_name_by_path(module_location)
    class_names = list()

    if module_location.endswith('__init__.py'):
        pythonpathsetter.add_path(os.path.dirname(module_directory))
        module = _load_module(module_full_name, module_location)
        class_names.extend(_find_names_in_module(module, module_full_name))
        class_names.extend(_find_names_in_path([module_directory], module_full_name))

    elif module_location.endswith('.py'):
        pythonpathsetter.add_path(module_directory)
        module = _load_module(module_full_name, module_location)
        class_names.extend(_find_names_in_module(module, module_full_name))

    elif module_location.endswith(".zip") or module_location.endswith(".jar"):
        pythonpathsetter.add_path(module_location)
        class_names.extend(_find_names_in_path([module_location], module_full_name))

    else:
        raise Exception('Unrecognized library path: ' + module_location)
    
    return sorted(set(class_names))


def _find_names_in_path(path, module_full_name):
    import pkgutil
    
    class_names = list()
    for loader, name, _ in pkgutil.walk_packages(path):
        try:
            module = loader.find_module(name).load_module(name)
            class_names.extend(_find_names_in_module(module, module_full_name))
        except:
            pass  # some modules can't be loaded  separately
    return class_names    


# FIXME: check if such import fallback is needed & unify it with imports in red_modules.py and red_libraries.py
def _load_module(module_name, module_location):
    import importlib
    try:
        return importlib.import_module(module_name)
    except Exception as e:
        _extend_sys_path(module_location)
        return importlib.import_module(module_name)


def _extend_sys_path(start_path):
    import sys
    import pkgutil
    from robot import pythonpathsetter

    path_walk = start_path if start_path in sys.path else _find_module_path(start_path)
    for loader, _, _ in pkgutil.walk_packages([path_walk]):
        pythonpathsetter.add_path(loader.path)


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


def _find_names_in_module(module, module_full_name):
    import inspect

    result = list()
    if module.__name__.startswith(module_full_name):
        result.append(module.__name__)
    else:
        result.append(module_full_name + "." + module.__name__)
    for _, obj in inspect.getmembers(module):
        if inspect.isclass(obj) and obj.__module__.startswith(module_full_name):
            result.append(obj.__module__ + "." + obj.__name__)

    return result


if __name__ == '__main__':
    import sys
    import json

    module_location = sys.argv[1]
    module_name = None

    if len(sys.argv) > 2:
        if sys.argv[2] == '-modulename':
            module_name = sys.argv[3]
            if len(sys.argv) > 4:
                sys.path.extend(sys.argv[4].split(';'))
        else:
            sys.path.extend(sys.argv[2].split(';'))

    print(json.dumps(get_classes_from_module(module_location, module_name)))
