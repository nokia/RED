#
# Copyright 2017 Nokia Solutions and Networks
# Licensed under the Apache License, Version 2.0,
# see license.txt file for details.
#

import threading
import sys
import json
import types
import inspect
import re

from robot.api import SuiteVisitor
from robot.running import TestLibrary
from robot.running.testlibraries import _BaseTestLibrary
from robot.running.handlers import _DynamicHandler, _JavaHandler
from robot.output import LOGGER, Message


class SuiteVisitorImportProxy(SuiteVisitor):

    LIB_IMPORT_TIMEOUT = 60

    def __init__(self, handle_keywords=False):
        import robot.running.namespace
        robot.running.namespace.IMPORTER = RedImporter(robot.running.namespace.IMPORTER, self.LIB_IMPORT_TIMEOUT,
                                                       handle_keywords)

    def visit_suite(self, suite):
        suite.tests.clear()
        suite.keywords.clear()
        suite.suites.visit(self)

    def visit_test(self, test):
        # test visiting skipped
        pass

    def visit_keyword(self, kw):
        # keyword visiting skipped
        pass

    def visit_message(self, msg):
        # message visiting skipped
        pass


class RedImporter(object):
    def __init__(self, importer, lib_import_timeout, handle_keywords=False):
        self.importer = importer
        self.lib_import_timeout = lib_import_timeout
        self.handle_keywords = handle_keywords
        self.func = None
        self.lock = threading.Lock()
        self.cached_lib_items = list()
        self.cached_kw_items = set()

    def __getattr__(self, name):
        self.lock.acquire()
        try:
            if hasattr(self.importer, name):
                func = getattr(self.importer, name)
                return lambda *args, **kwargs: self._wrap(func, args, kwargs)
            raise AttributeError(name)
        finally:
            self.lock.release()

    def _wrap(self, func, args, kwargs):
        if isinstance(func, types.MethodType):
            if func.__name__ == 'import_library':
                return self._handle_lib_import(func, args, kwargs)
            else:
                return func(*args, **kwargs)
        else:
            return func(self.importer, *args, **kwargs)

    def _handle_lib_import(self, func, args, kwargs):
        libs = []
        errors = []
        lib_cached = self._get_lib_from_cache(args[0], args[1])
        if lib_cached:
            libs.append(lib_cached.lib)
            errors = lib_cached.errors
        else:
            try:
                def to_call():
                    try:
                        libs.append(func(*args, **kwargs))
                    except:
                        errors.append(sys.exc_info())

                t = threading.Thread(target=to_call)
                t.setDaemon(True)
                t.start()
                t.join(timeout=self.lib_import_timeout)
            except:
                errors.append(sys.exc_info())

        if len(libs) > 0:
            library = libs[0]
        else:
            try:
                library = TestLibrary(args[0], args[1], args[2], create_handlers=False)
            except:
                try:
                    library = _BaseTestLibrary(libcode=None, name=args[0], args=args[1], source=None, variables=args[2])
                except:
                    try:
                        library = _BaseTestLibrary(libcode=None, name=args[0], args=[], source=None, variables=args[3])
                    except:
                        errors.append(sys.exc_info())

        if lib_cached is None:
            self.cached_lib_items.append(LibItem(args[0], args[1], library, errors))

        for e in errors:
            msg = json.dumps({'import_error': {'name': args[0], 'error': str(e)}})
            LOGGER.message(Message(message=msg, level='NONE'))

        if self.handle_keywords:
            self._handle_keywords(library)

        return library

    def _get_lib_from_cache(self, name, args):
        for cached_lib in self.cached_lib_items:
            if cached_lib.name == name:
                if len(cached_lib.args) == len(args):
                    for cached_arg, arg in zip(cached_lib.args, args):
                        if cached_arg != arg:
                            return None
                    return cached_lib
        return None

    def _handle_keywords(self, library):
        if library and hasattr(library, 'handlers'):
            for keyword in library.handlers:
                if keyword not in self.cached_kw_items and not isinstance(keyword, _JavaHandler):
                    try:
                        keyword_source = PythonKeywordSource(keyword)
                        msg = json.dumps({'keyword_source': dict(keyword_source.__dict__)})
                        LOGGER.message(Message(message=msg, level='NONE'))
                    except:
                        pass  # TODO: add logging
                    finally:
                        self.cached_kw_items.add(keyword)


class LibItem(object):
    def __init__(self, name, args, lib=None, errors=list()):
        self.name = name
        self.args = args
        self.lib = lib
        self.errors = errors


class PythonKeywordSource(object):
    def __init__(self, keyword):
        self.name = keyword.name
        self.libraryName = keyword.library.name
        source = self._find_source(keyword)
        self.filePath = source[0]
        self.line = source[1]
        self.offset = source[2]
        self.length = source[3]

    def _find_source(self, keyword):
        function = self._resolve_function(keyword)
        path = inspect.getfile(function)
        source = inspect.getsourcelines(function)
        for lineIdx, line in enumerate(source[0]):
            m = re.search('(?<=def)(\s*)([^ \t\n\r\f\v(]+)', line)
            if m is not None:
                line = source[1] + lineIdx - 1
                offset = m.start(2)
                length = len(m.group(2))
                return path, line, offset, length
        return path, 0, 0, 0

    @staticmethod
    def _resolve_function(keyword):
        if isinstance(keyword, _DynamicHandler):
            return keyword.library._libcode.__dict__[keyword._run_keyword_method_name]
        elif keyword._method:
            return keyword._method
        else:
            return keyword._get_handler(keyword.library.get_instance(), keyword._handler_name)
