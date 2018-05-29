import unittest
import os

from red_modules import get_modules_search_paths
from red_modules import get_module_path


class MudulePathsRetrievingTests(unittest.TestCase):
    def test_if_modules_search_paths_are_returned(self):
        result = get_modules_search_paths()

        self.assertTrue(len(result) > 0)

    def test_if_exception_is_raised_when_module_path_cannot_be_found(self):
        with self.assertRaises(ImportError):
            get_module_path('UnknownModuleName')

    def test_if_python_module_path_is_returned(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))
        module_path = os.path.join(parent_path, 'res_test_red_modules')

        import importlib
        importlib.import_module('res_test_red_modules')

        result = get_module_path('res_test_red_modules')

        self.assertEqual(result, module_path)

    def test_if_jar_module_path_is_returned_for_java_class(self):
        import platform
        if 'Jython' in platform.python_implementation():
            parent_path = os.path.dirname(os.path.realpath(__file__))
            module_path = os.path.join(parent_path, 'res_test_red_modules', 'SampleLib.jar')

            from classpath_updater import ClassPathUpdater
            cp_updater = ClassPathUpdater()
            cp_updater.add_file(module_path)
            result = get_module_path('ExampleLibrary')

            from java.io import File
            self.assertEqual(File(result).getAbsolutePath(), File(module_path).getAbsolutePath())

    def test_if_jar_module_path_is_returned_for_python_module(self):
        import platform
        if 'Jython' in platform.python_implementation():
            parent_path = os.path.dirname(os.path.realpath(__file__))
            module_path = os.path.join(parent_path, 'res_test_red_modules', 'SamplePythonLib.jar')

            from classpath_updater import ClassPathUpdater
            cp_updater = ClassPathUpdater()
            cp_updater.add_file(module_path)
            result = get_module_path('ExamplePythonLibrary')

            from java.io import File
            self.assertEqual(File(result).getAbsolutePath(), File(module_path).getAbsolutePath())
