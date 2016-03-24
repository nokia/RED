from __future__ import with_statement
import robot
from robot.output import LOGGER, Message
from robot.api import SuiteVisitor
from robot.running.namespace import Namespace
import robot.running.importer
from robot.running import TestLibrary
from types import MethodType
from threading import Lock
import sys
import threading

class SuiteVisitatorImportProxy(SuiteVisitor):
    def __init__(self, lib_import_timeout=5):
        self.lib_import_timeout = int(lib_import_timeout)
        robot.running.namespace.IMPORTER = MyIMPORTER(robot.running.namespace.IMPORTER, self.lib_import_timeout)

class MyIMPORTER(object):
    def __init__(self, obj, lib_import_timeout):
        self.obj = obj
        self.lib_import_timeout = int(lib_import_timeout)
        self.func = None
        self.lock = Lock()

    def __getattr__(self, name):
        self.lock.acquire()
        try:
            if hasattr(self.obj, name):
                func = getattr(self.obj, name)
                return lambda *args, **kwargs: self._wrap(func, args, kwargs)
            raise AttributeError(name)
        finally:
            self.lock.release()


    def _wrap(self, func, argser, kwargs):
        if type(func) == MethodType:
            if func.__name__ == 'import_library':
                #print str(argser)
                q = []
                errors = []
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
                    result = TestLibrary(argser[0], argser[1], argser[2], create_handlers=False)

                for p in errors:
                    msg = Message(message='{LIB_ERROR: ' + argser[0] + ', value: VALUE_START(' + str(p) + ')VALUE_END, lib_file_import:' +
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







