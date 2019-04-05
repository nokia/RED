#
# Copyright 2017 Nokia Solutions and Networks
# Licensed under the Apache License, Version 2.0,
# see license.txt file for details.
#


import os
import importlib
import pkgutil
import inspect
import code

from robot import pythonpathsetter


def get_classes_from_module(module_location):
    module_directory = os.path.dirname(module_location)
    module_name = _find_module_name_by_path(module_location)
    module_names_collector = _get_module_names_collector()
    class_names = set()

    if module_location.lower().endswith('__init__.py'):
        pythonpathsetter.add_path(os.path.dirname(module_directory))
        inside_file_names = module_names_collector._try_to_find_names_in_module(module_name, module_location)
        inside_file_names.extend(module_names_collector._try_to_find_names_in_path([module_directory], _find_names_in_module))
        class_names.update(_combine_module_name_parts(inside_file_names, module_name))

    elif module_location.lower().endswith('.py'):
        pythonpathsetter.add_path(module_directory)
        inside_file_names = module_names_collector._try_to_find_names_in_module(module_name, module_location)
        class_names.update(_combine_module_name_parts(inside_file_names, module_name))

    elif isJarOrZip(module_location):
        pythonpathsetter.add_path(module_location)
        class_names.update(module_names_collector._try_to_find_names_in_archive([module_location], _find_names_in_archive_module))

    else:
        raise Exception('Unrecognized library path: ' + module_location)

    return sorted(class_names)


def _get_module_names_collector():
    import platform
    if 'Jython' in platform.python_implementation():
        return ModuleNamesCollectorForJython()
    if platform.python_version_tuple()[0] == '3':
        return ModuleNamesCollectorForPython3()
    else:
        return ModuleNamesCollectorForPython2()


def _combine_module_name_parts(modules_inside, module_name):
    result = set()
    pre_index = module_name
    while True:
        result.add(pre_index)
        for inside in modules_inside:
            result.add(pre_index + '.' + inside)
        if '.' in pre_index:
            pre_index = pre_index.split('.', 1)[-1]
        else:
            return result


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

def _try_to_find_names_in_archive(path, names_finder):
    result = list()
    for loader, name, _ in pkgutil.walk_packages(path):
        try:
            module = loader.find_module(name, path).load_module(name)
            result.extend(names_finder(module, name))
        except:
            pass  # some modules can't be loaded  separately

    return result


def isJarOrZip(module_location):
    return module_location.lower().endswith(".jar") or module_location.lower().endswith(".zip")


class ModuleNamesCollectorForPython2():
    def _try_to_find_names_in_module(self, module_name, module_location):
        with open(module_location) as f:
            path = module_location.encode('utf-8') if isinstance(module_location, unicode) else module_location
            code = compile(f.read(), path, 'exec')
        return self._extract_names_from_code_object_for_python2(code)
    
    
    def _extract_names_from_code_object_for_python2(self, code):
        result = list()
        try:
            code_parts = code.co_consts
            for i in range(0, len(code_parts)):
                if isinstance(code_parts[i], str) and inspect.iscode(code_parts[i + 1]) and code_parts[i + 1].co_name == code_parts[i]:
                    result.append(code_parts[i])
            return result
        except:
            return result
    
    
    def _try_to_find_names_in_path(self, path, names_finder=None, prefix=''):
        result = list()
        for loader, name, _ in pkgutil.walk_packages(path):
            try:
                module = loader.find_module(name, path)
                result.append(prefix + name)
                if os.path.isdir(module.filename):
                    result.extend(self._try_to_find_names_in_path([module.filename], _find_names_in_module, prefix + module.fullname + '.'))
                else:
                    in_file_result = list()
                    if module.filename.endswith('.py'):
                        in_file_result = self._try_to_find_names_in_module(module.fullname, module.filename)
                    else:
                        in_file_result = _try_to_find_names_in_archive([module.filename], _find_names_in_archive_module)
                    result.extend([prefix + name + '.' + n for n in in_file_result])
            except:
                pass  # some modules can't be loaded  separately
        return result


    def _try_to_find_names_in_archive(self, path, names_finder):
        return _try_to_find_names_in_archive(path, names_finder)
    
    
class ModuleNamesCollectorForPython3():
    def _try_to_find_names_in_module(self, module_name, module_location):
        with open(module_location, 'r', encoding='utf-8') as f:
            code = compile(str(bytes(f.read(), 'utf-8'), 'utf-8'), module_location, 'exec')
        return self._extract_names_from_code_object_for_python3(code)


    def _extract_names_from_code_object_for_python3(self, code):
        result = list()
        try:
            code_parts = code.co_consts
            for i in range(0, len(code_parts)):
                if inspect.iscode(code_parts[i]) and isinstance(code_parts[i + 1], str) and code_parts[i].co_names and \
                        code_parts[i].co_name == code_parts[i + 1] and code_parts[i].co_consts[0] is not None:
                    result.append(code_parts[i + 1])
        except:
            return result
        return result


    def _try_to_find_names_in_path(self, path, names_finder=None, prefix=''):
        import importlib.util
        result = list()
        for loader, name, _ in pkgutil.walk_packages(path):
            try:
                module = importlib.util.module_from_spec(importlib.util._find_spec_from_path(name, path))
                result.append(prefix + name)
                if module.__file__.endswith('__init__.py'):
                    result.extend(self._try_to_find_names_in_path([os.path.dirname(module.__file__)], _find_names_in_module, prefix + module.__name__ + '.'))
                else:
                    in_file_result = list()
                    if module.__file__.endswith('.py'):
                        in_file_result = self._try_to_find_names_in_module(module.__name__, module.__file__)
                    else:
                        in_file_result = _try_to_find_names_in_archive([module.__file__], _find_names_in_archive_module)
                    result.extend([prefix + name + '.' + n for n in in_file_result])
            except:
                pass  # some modules can't be loaded  separately
        return result


    def _try_to_find_names_in_archive(self, path, names_finder):
        return _try_to_find_names_in_archive(path, names_finder)


class ModuleNamesCollectorForJython():
    def _try_to_find_names_in_module(self, module_name, module_location=None):
        # Because jython does not support co_consts in code object we need to try
        # to import every option instead of compiling file like for other python versions
        # Moreover - this may cause old bugs with same lib names to appear for jython
        # For more info see Jira 'RED-928' and related
        try:
            module = importlib.import_module(module_name)
            return self._find_names_in_module_for_jython(module, module_name)
        except:
            module_name_base = module_name.rsplit('.', 1)[-1]
            if module_name == module_name_base:
                raise
            module = importlib.import_module(module_name_base)
            return self._find_names_in_module_for_jython(module, module_name_base)
    
    
    def _find_names_in_module_for_jython(self, module, name):
        result = list()
        for _, obj in inspect.getmembers(module):
            if inspect.isclass(obj) and obj.__module__.startswith(name):
                result.append(obj.__name__)
    
        return result
    
    
    def _try_to_find_names_in_path(self, path, names_finder):
        # using path for jython does not work properly
        result = list()
        for loader, name, _ in pkgutil.walk_packages(path):
            try:
                module = loader.find_module(name).load_module(name)
                result.extend(names_finder(module, name))
            except:
                pass  # some modules can't be loaded  separately
        return result
    
    def _try_to_find_names_in_archive(self, path, names_finder):
        return self._try_to_find_names_in_path(path, names_finder)