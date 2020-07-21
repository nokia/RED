#
# Copyright 2016 Nokia Solutions and Networks
# Licensed under the Apache License, Version 2.0,
# see license.txt file for details.
#


def get_standard_library_names():
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


def get_site_packages_libraries_names():
    robot_libs = list()
    non_robot_libs = list()

    try:
        from pip._internal.utils.misc import get_installed_distributions
    except ImportError:  # for pip<10
        from pip import get_installed_distributions

    try:
        for package in get_installed_distributions():
            metadata = list(package._get_metadata("top_level.txt"))
            if metadata:
                if not metadata[0].startswith('_'):
                    if 'robotframework-' in package.key:
                        robot_libs.append(metadata[0])
                    else:
                        non_robot_libs.append(metadata[0])
        return robot_libs, non_robot_libs
    except:
        return robot_libs, non_robot_libs


def create_libdoc(libname, format):
    from tempfile import mkstemp
    from base64 import b64encode
    import os
    import sys
    
    is_py2 = sys.version_info < (3, 0, 0)

    try:
        f, temp_lib_file_path = mkstemp()
        os.close(f)
        console_output = _create_libdoc_with_stdout_redirect(libname, format, temp_lib_file_path)

        # check if anything was written into the file i.e. specification was generated        
        if os.stat(temp_lib_file_path).st_size > 0:
            if format == 'xml':
                content = _switch_source_to_absolute(temp_lib_file_path, 'utf-8' if is_py2 else 'unicode')
            else:
                content = _read_content(temp_lib_file_path, is_py2)
            content = b64encode(content) if is_py2 else str(b64encode(bytes(content, 'utf-8')), 'utf-8')
            return content
        else:
            raise Exception(console_output)
    finally:
        os.remove(temp_lib_file_path)


def _read_content(temp_lib_file_path, is_py2):
    if is_py2:
        with open(temp_lib_file_path, 'r') as lib_file:
            return lib_file.read()
    else:
        with open(temp_lib_file_path, 'r', encoding='utf-8') as lib_file:
            return lib_file.read()


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


def _switch_source_to_absolute(filepath, encoding):
    # robot will usually write relative path to source of
    # keyword/library but we want it to be absolute
    from xml.etree import ElementTree
    from os.path import isabs, dirname, normpath, join
    
    tree = ElementTree.parse(filepath)
    root = tree.getroot()

    all_with_source = [root]
    all_with_source.extend(root.findall('kw'))

    dir_path = dirname(filepath)
    for tag in all_with_source:
        if tag.get('source') is not None and not isabs(tag.get('source')):
            abs_path = normpath(join(dir_path, tag.get('source')))
            tag.set('source', abs_path)
            
    return '<?xml version="1.0" encoding="UTF-8"?>\n' + ElementTree.tostring(root, encoding)


def create_html_doc(doc, format):
    from robot.libdocpkg.htmlwriter import DocToHtml

    formatter = DocToHtml(format)
    return formatter(doc)


if __name__ == '__main__':
    import robot_session_server
    import sys

    decoded_args = robot_session_server.__decode_unicode_if_needed(sys.argv)

    libname = decoded_args[1]
    format = decoded_args[2]
    python_paths = decoded_args[3].split(";")
    class_paths = decoded_args[4].split(";") if len(decoded_args) == 5 else []

    robot_session_server.__extend_paths(python_paths, class_paths)

    print("Libdoc >" + create_libdoc(libname, format))
