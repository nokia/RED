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
    _, path, _ = imp.find_module(modulename)
    return path

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