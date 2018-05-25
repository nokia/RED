#
# Copyright 2016 Nokia Solutions and Networks
# Licensed under the Apache License, Version 2.0,
# see license.txt file for details.
#


def get_modules_search_paths():
    import sys
    import robot  # Robot will add some paths to PYTHONPATH
    return sys.path


def get_module_path(module_name):
    import imp
    try:
        _, path, _ = imp.find_module(module_name)
        return path
    except ImportError as e:
        # when trying to locate jar modules via jython
        import platform
        if platform.python_implementation() != 'Jython':
            raise e

        import sys
        path_hook_finder = sys.path_importer_cache['__classpath__']
        if path_hook_finder:
            m = path_hook_finder.load_module(module_name)
            map(__import__, [module_name])
            if m is None:
                m = path_hook_finder.load_module(module_name)

            jar_path = m.getResource('/' + m.getName().replace('.', '/') + ".class").getPath()
            if jar_path is None or len(jar_path) == 0:
                raise e

            if jar_path.startswith('file:/'):
                jar_path = jar_path[6:]
            if jar_path.index('.jar!') > 0:
                jar_path = jar_path[:jar_path.index('.jar!')] + '.jar'
            return jar_path
        raise e


if __name__ == '__main__':
    import sys
    import json

    if sys.argv[1] == '-pythonpath':
        print(json.dumps(get_modules_search_paths()))
    elif sys.argv[1] == '-modulename':
        module_name = sys.argv[2]

        if len(sys.argv) > 3:
            sys.path.extend(sys.argv[3].split(';'))

        print(get_module_path(module_name))
    else:
        raise Exception('Unrecognized argument:' + sys.argv[1])
