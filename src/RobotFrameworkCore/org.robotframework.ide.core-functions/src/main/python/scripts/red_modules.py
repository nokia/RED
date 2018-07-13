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
        if 'Jython' in platform.python_implementation():
            source = _find_jar_source_path(module_name)
            if source:
                if 'file:' in source:
                    source = source[source.index('file:') + 5:]

                if '.jar!' in source:
                    source = source[:source.rindex('.jar!')] + '.jar'
                elif '.jar/' in source:
                    source = source[:source.rindex('.jar/')] + '.jar'
                elif '.jar\\' in source:
                    source = source[:source.rindex('.jar\\')] + '.jar'

                return source
        raise e


def _find_jar_source_path(module_name):
    import org.python.core.imp as jimp
    from types import ModuleType
    module = jimp.load(module_name)
    if isinstance(module, ModuleType):
        source = module.__file__
        if '__pyclasspath__' in source:
            res = source[source.index('__pyclasspath__') + 16:]
            return jimp.getSyspathJavaLoader().getResource(res).getPath()
        return source
    else:
        return module.getResource('/' + module_name.replace('.', '/') + ".class").getPath()
