#
# Copyright 2017 Nokia Solutions and Networks
# Licensed under the Apache License, Version 2.0,
# see license.txt file for details.
#


def get_classes_from_module(module_location, module_name):
    import os
    import pkgutil

    if module_location.endswith('__init__.py'):
        def to_call():
            module_path = os.path.dirname(module_location)
            module_name_from_location = os.path.basename(module_path)
            loaded_module = _load_module(module_location, module_name, module_name_from_location)
            class_names = _find_names_in_module(loaded_module, module_name_from_location)
            for loader, name, _ in pkgutil.walk_packages([module_path]):
                loaded_module = loader.find_module(name).load_module(name)
                class_names.extend(_find_names_in_module(loaded_module, module_name_from_location))
            return _get_names_combinations(class_names, _get_module_name_by_path(module_location))

        return _call_with_extended_sys_path(to_call, os.path.dirname(os.path.dirname(module_location)))

    elif module_location.endswith('.py'):
        def to_call():
            module_name_from_location = os.path.basename(module_location)[:-3]
            loaded_module = _load_module(module_location, module_name, module_name_from_location)
            class_names = _find_names_in_module(loaded_module, module_name_from_location)
            return _get_names_combinations(class_names, _get_module_name_by_path(module_location))

        return _call_with_extended_sys_path(to_call, os.path.dirname(module_location))

    elif module_location.endswith(".zip") or module_location.endswith(".jar"):
        def to_call():
            class_names = list()
            for loader, name, _ in pkgutil.walk_packages([module_location]):
                loaded_module = loader.load_module(name)
                map(__import__, [name])
                class_names.extend(_find_names_in_archive_module(loaded_module, name))
            return class_names

        return _call_with_extended_sys_path(to_call, module_location)

    else:
        raise Exception('Unrecognized library path: ' + module_location)


def _call_with_extended_sys_path(to_call, path):
    import sys
    from robot import pythonpathsetter

    old_sys_path = list(sys.path)

    pythonpathsetter.add_path(path)

    try:
        return to_call()
    except:
        raise
    finally:
        sys.path = old_sys_path


def _load_module(module_location, module_name, module_name_from_location):
    try:
        import imp
        module_file, module_path, module_desc = imp.find_module(module_name_from_location)
        return imp.load_module(module_name_from_location, module_file, module_path, module_desc)
    except Exception as e:
        _extend_sys_path(module_location)

        import importlib
        try:
            if module_name:
                return importlib.import_module(module_name)
            else:
                return importlib.import_module(module_name_from_location)
        except Exception as e:
            return importlib.import_module(_get_module_name_by_path(module_location))


def _extend_sys_path(start_path):
    import sys
    import pkgutil
    from robot import pythonpathsetter

    path_walk = start_path if start_path in sys.path else _find_module_path(start_path)
    for loader, _, _ in pkgutil.walk_packages([path_walk]):
        pythonpathsetter.add_path(loader.path)


def _find_module_path(start_path):
    import os

    result = os.path.abspath(start_path.split(os.sep)[0])

    do = True
    current_path = os.path.abspath(start_path)
    while do:
        if os.path.isfile(current_path):
            current_path = os.path.dirname(current_path)
        elif os.path.isdir(current_path):
            import sys
            if os.path.exists(current_path + os.sep + '__init__.py') or os.path.basename(current_path) in sys.modules:
                current_path = os.path.normpath(os.path.join(current_path, os.path.pardir))
            else:
                result = current_path
                do = False
                break
        else:
            do = False
            break

    return result


def _get_module_name_by_path(start_path):
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
            result = result[:-3]

    return result


def _get_names_combinations(names, module_name):
    for mod_index in range(len(names) - 1, 0, -1):
        if '.' in names[mod_index]:
            names[mod_index] = names[mod_index].split(".", 1)[1] 
    result = list(names)

    if len(names) == 1 and names[0] == module_name:
        pass
    else:
        module_names = module_name.split('.')[:-1]
        if len(module_names) > 0:
            for mod_index in range(len(module_names) - 1, -1, -1):
                pre_index = '.'.join(module_names[mod_index:])
                for get_index in range(0, len(names)):
                    if not pre_index + '.' + names[get_index] in result:
                        result.append(pre_index + '.' + names[get_index])
                    

    for mod_index in range(len(names) - 1, 0, -1):
        if names[mod_index] in result: result.remove(names[mod_index])

    return result


def _find_names_in_module(module, name):
    import inspect

    result = list()
    result.append(module.__name__)
    for n, obj in inspect.getmembers(module):
        if inspect.isclass(obj) and obj.__module__.startswith(name):
            result.append(obj.__module__ + '.' + obj.__name__)

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
    module_name = None

    if len(sys.argv) > 2:
        if sys.argv[2] == '-modulename':
            module_name = sys.argv[3]
            if len(sys.argv) > 4:
                sys.path.extend(sys.argv[4].split(';'))
        else:
            sys.path.extend(sys.argv[2].split(';'))

    print(json.dumps(get_classes_from_module(module_location, module_name)))
