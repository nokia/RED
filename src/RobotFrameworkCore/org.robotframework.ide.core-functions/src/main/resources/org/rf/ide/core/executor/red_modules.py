#
# Copyright 2016 Nokia Solutions and Networks
# Licensed under the Apache License, Version 2.0,
# see license.txt file for details.
#

def get_modules_search_paths():
    import sys
    import robot  # Robot will add some paths to PYTHONPATH
    return sys.path

def get_module_path(modulename):
    import imp
    try:
        _, path, _ = imp.find_module(modulename)
        return path
    except ImportError as e:
        # when trying to locate jar modules via jython
        import sys
        for path_hook in sys.path_hooks:
            if str(path_hook).find('org.python.core.JavaImporter') != -1:
                m = path_hook.load_module(modulename)
                map(__import__, [modulename])
                import inspect
                return inspect.getsourcefile(m)
        raise e

def get_run_module_path():
    import robot
    return robot.__file__


if __name__ == '__main__':
    import sys
    import json

    if sys.argv[1] == '-pythonpath':
        print(json.dumps(get_modules_search_paths()))
    elif sys.argv[1] == '-modulepath':
        print(json.dumps(get_module_path(sys.argv[2])))
    elif sys.argv[1] == '-runmodulepath':
        print(get_run_module_path())