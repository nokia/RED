from __future__ import with_statement
import robot
from robot.output import LOGGER, Message
from robot.api import SuiteVisitor
from robot.running.model import TestCase, Keyword
from robot.running.namespace import Namespace
import robot.running.importer
from robot.running import TestLibrary
from robot.running.testlibraries import _BaseTestLibrary
from types import MethodType
from threading import Lock
from robot.running.builder import TestSuiteBuilder
from robot.run import RobotFramework
from robot.conf import RobotSettings
import time
import sys
import threading


class MyTestSuiteBuilder(TestSuiteBuilder):
    ''' switch off empty suite removing '''

    def _parse_and_build(self, path):
        suite = self._build_suite(self._parse(path))
        return suite


class SuiteVisitorImportProxy(SuiteVisitor):
    def __init__(self, lib_import_timeout=5):
        self.lib_import_timeout = int(lib_import_timeout)
        robot.running.namespace.IMPORTER = MyIMPORTER(robot.running.namespace.IMPORTER, self.lib_import_timeout)
        self.options, self.arguments = RobotFramework().parse_arguments(sys.argv[1:])
        self.settings = RobotSettings(**self.options)
        self.f_suites = self.settings.suite_config['include_suites']

    def start_suite(self, suite):
        if suite:
            if suite.parent:
                suite.parent.keywords.clear()
                suite.tests.clear()
                t = TestCase(name='Fake_' + str(int(round(time.time() * 1000))))
                suite.tests.append(t)
            else:
                if len(suite.tests) == 0 or suite.test_count == 0:
                    current_suite = MyTestSuiteBuilder().build(suite.source)
                    if len(self.f_suites) == 0:
                        suite.suites = current_suite.suites
                    else:
                        suite.suites = self.filter_by_name(current_suite.suites)

            suite.keywords.clear()

    def filter_by_name(self, suites):
        matched_suites = []

        for suite in suites:
            for s_name in self.f_suites:
                longpath = suite.longname.lower().replace('_', ' ')
                normalized_s_name = s_name.lower().replace('_', ' ')
                meet = False
                if (len(longpath) >= len(normalized_s_name) and longpath.startswith(normalized_s_name)):
                    meet = True
                    after_remove = longpath.replace(normalized_s_name, '')
                elif (len(longpath) < len(normalized_s_name) and normalized_s_name.startswith(longpath)):
                    meet = True
                    after_remove = normalized_s_name.replace(longpath, '')

                if meet and (after_remove == '' or after_remove.startswith('.') or after_remove.startswith(
                        '*') or after_remove.startswith('?')):
                    matched_suites.append(suite)
                    suite.suites = self.filter_by_name(suite.suites)

        return matched_suites

    def start_test(self, test):
        if test:
            test.name = 'Fake_' + str(int(round(time.time() * 1000)))
            test.keywords.clear()
            test.keywords.append(Keyword(name='BuiltIn.No Operation'))


class LibItem(object):
    def __init__(self, name, args):
        self.name = name
        self.args = args
        self.result = None
        self.errors = list()

    def get_result_test_object(self):
        return self.result


class MyIMPORTER(object):
    def __init__(self, obj, lib_import_timeout):
        self.obj = obj
        self.lib_import_timeout = int(lib_import_timeout)
        self.func = None
        self.lock = Lock()
        self.cached_lib_items = list()

    def __getattr__(self, name):
        self.lock.acquire()
        try:
            if hasattr(self.obj, name):
                func = getattr(self.obj, name)
                return lambda *args, **kwargs: self._wrap(func, args, kwargs)
            raise AttributeError(name)
        finally:
            self.lock.release()

    def get_from_cache(self, name, args):
        result = None
        for cached_lib in self.cached_lib_items:
            if cached_lib.name == name:
                if len(cached_lib.args) == len(args):
                    correct = True
                    arg_size = len(cached_lib.args)
                    for arg_id in range(0, arg_size):
                        if cached_lib.args[arg_id] != args[arg_id]:
                            correct = False
                            break
                    if correct:
                        result = cached_lib
                        break
        return result

    def _wrap(self, func, argser, kwargs):
        if type(func) == MethodType:
            if func.__name__ == 'import_library':
                # print str(argser)
                q = []
                errors = []
                lib_cached = self.get_from_cache(argser[0], argser[1])
                if lib_cached:
                    q.append(lib_cached.result)
                    errors = lib_cached.errors
                else:
                    try:
                        t = threading.Thread(target=self._imp, args=(func, q, errors, argser), kwargs=kwargs)
                        t.setDaemon(True)
                        t.start()
                        t.join(timeout=int(self.lib_import_timeout))
                    except:
                        errors.append(sys.exc_info())
                if len(q) > 0:
                    result = q[0]
                else:
                    try:
                        result = TestLibrary(argser[0], argser[1], argser[2], create_handlers=False)
                    except:
                        try:
                            result = _BaseTestLibrary(libcode=None, name=argser[0], args=argser[1], source=None,
                                                      variables=argser[2])
                        except:
                            try:
                                result = _BaseTestLibrary(libcode=None, name=argser[0], args=[], source=None, variables=argser[3])
                            except:
                                errors.append(sys.exc_info())

                if lib_cached is None:
                    lib = LibItem(argser[0], argser[1])
                    lib.result = result
                    lib.errors = errors
                    self.cached_lib_items.append(lib)

                for p in errors:
                    msg = Message(message='{LIB_ERROR: ' + argser[0] + ', value: VALUE_START(' + str(
                        p) + ')VALUE_END, lib_file_import:' +
                                          str(result.source) + '}', level='FAIL')
                    LOGGER.message(msg)
            else:
                result = func(*argser, **kwargs)
        else:
            result = func(self.obj, *argser, **kwargs)

        return result

    def _imp(self, func, q, errors, args, kwargs={}):
        try:
            res = func(*args, **kwargs)
            q.append(res)
        except:
            ''' (type, value, traceback) '''
            errors.append(sys.exc_info())
