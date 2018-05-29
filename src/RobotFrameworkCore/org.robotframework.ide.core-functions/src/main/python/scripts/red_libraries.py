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
    source = module.__file__
    if source.endswith('.pyc'):
        source = source[:-1]
    elif source.endswith('$py.class'):
        source = source[:-9] + '.py'
    return source


def create_libdoc(libname, format):
    from tempfile import mkstemp
    import os

    try:
        f, temp_lib_file_path = mkstemp()
        os.close(f)
        result = _create_libdoc_with_stdout_redirect(libname, format, temp_lib_file_path)
        encoded_libdoc = _encode_libdoc(temp_lib_file_path)
        if encoded_libdoc:
            return encoded_libdoc
        else:
            raise Exception(result)
    finally:
        os.remove(temp_lib_file_path)


def _create_libdoc_with_stdout_redirect(libname, format, temp_lib_file_path):
    from robot.libdoc import libdoc
    import sys
    try:
        from StringIO import StringIO
    except:
        from io import StringIO

    try:
        old_stdout = sys.stdout
        sys.stdout = StringIO()
        libdoc(libname, temp_lib_file_path, format=format)
        return sys.stdout.getvalue()
    finally:
        sys.stdout = old_stdout


def _encode_libdoc(temp_lib_file_path):
    from base64 import b64encode
    import sys
    if sys.version_info < (3, 0, 0):
        with open(temp_lib_file_path, 'r') as lib_file:
            return b64encode(lib_file.read())
    else:
        with open(temp_lib_file_path, 'r', encoding='utf-8') as lib_file:
            return str(b64encode(bytes(lib_file.read(), 'utf-8')), 'utf-8')


def create_html_doc(doc, format):
    from robot.libdocpkg.htmlwriter import DocToHtml

    formatter = DocToHtml(format)
    return formatter(doc)


if __name__ == '__main__':
    import sys
    import json

    if sys.argv[1] == '-names':
        print(json.dumps(get_standard_library_names()))
    elif sys.argv[1] == '-path':
        print(get_standard_library_path(sys.argv[2]))
    elif sys.argv[1] == '-libdoc':
        libname = sys.argv[2]
        format = sys.argv[3]
        paths = sys.argv[4:]

        sys.path = paths + sys.path
        print(create_libdoc(libname, format))
    elif sys.argv[1] == '-htmldoc':
        format = sys.argv[2]
        filepath = sys.argv[3]

        if sys.version_info < (3, 0, 0):
            with open(filepath, 'r') as lib_file:
                doc = lib_file.read()
                print(create_html_doc(doc, format))
        else:
            with open(temp_lib_file_path, 'r', encoding='utf-8') as lib_file:
                doc = lib_file.read()
                print(create_html_doc(doc, format))
