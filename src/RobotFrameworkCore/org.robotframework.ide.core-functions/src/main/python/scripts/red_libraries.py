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


def create_libdoc(libname, format):
    from robot.libdoc import libdoc
    from tempfile import mkstemp
    from base64 import b64encode
    import sys
    import os

    try:
        f, temp_lib_file_path = mkstemp()
        os.close(f)
        libdoc(libname, temp_lib_file_path, format=format)
        if sys.version_info < (3, 0, 0):
            with open(temp_lib_file_path, 'r') as lib_file:
                return b64encode(lib_file.read())
        else:
            with open(temp_lib_file_path, 'r', encoding='utf-8') as lib_file:
                return str(b64encode(bytes(lib_file.read(), 'utf-8')), 'utf-8')
    finally:
        os.remove(temp_lib_file_path)


if __name__ == '__main__':
    import sys

    if sys.argv[1] == '-names':
        print('\n'.join(get_standard_library_names()))
    elif sys.argv[1] == '-path':
        print(get_standard_library_path(sys.argv[2]))
    elif sys.argv[1] == '-libdoc':
        libname = sys.argv[2]
        format = sys.argv[3]
        paths = sys.argv[4:]

        sys.path = paths + sys.path
        print(create_libdoc(libname, format))
