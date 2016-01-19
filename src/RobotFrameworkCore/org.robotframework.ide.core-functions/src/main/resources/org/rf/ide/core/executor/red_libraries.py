#
# Copyright 2016 Nokia Solutions and Networks
# Licensed under the Apache License, Version 2.0,
# see license.txt file for details.
#

def get_standard_library_names():
    try:
        import robot.running.namespace
        return list(robot.running.namespace.STDLIB_NAMES)
    except:
        # the std libraries set was moved to other place since Robot 2.9.1
        import robot.libraries
        return list(robot.libraries.STDLIBS)
    
def get_standard_library_path(libname):
    import importlib
    module = importlib.import_module('robot.libraries.' + libname)
    return module.__file__
    
if __name__ == '__main__':
    import sys
    if sys.argv[1] == '-names':
        print('\n'.join(get_standard_library_names()))
    elif sys.argv[1] == '-path':
        print(get_standard_library_path(sys.argv[2]))
     