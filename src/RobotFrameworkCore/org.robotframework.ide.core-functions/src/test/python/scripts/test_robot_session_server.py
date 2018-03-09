import unittest
import sys
import os

from robot_session_server import get_robot_version
from robot_session_server import create_libdoc
from robot_session_server import get_classes_from_module
from robot_session_server import get_module_path
from robot_session_server import get_variables


class RobotSessionServerTests(unittest.TestCase):
    def test_encode_result(self):
        response = get_robot_version()

        self.assertTrue(response['result'].startswith('Robot Framework '))
        self.assertEqual(response['exception'], None)

    def test_encode_exception(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))

        response = create_libdoc("LibError", 'XML', [os.path.join(parent_path, 'res_test_robot_session_server')], [])

        self.assertEqual(response['result'], None)
        self.assertTrue('Unable to generate library specification file for library: \'LibError\'' in response['exception'])


class LibdocGenerationTests(unittest.TestCase):
    def test_subsequent_calls_for_same_lib_name_under_different_paths_return_different_libdocs(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))

        response1 = create_libdoc("lib", 'XML', [os.path.join(parent_path, 'res_test_robot_session_server', 'a')], [])
        response2 = create_libdoc("lib", 'XML', [os.path.join(parent_path, 'res_test_robot_session_server', 'b')], [])

        self.assertNotEqual(response1, response2)

    def test_if_sys_path_is_not_extended_after_generating_libdoc(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))

        python_paths = [os.path.join(parent_path, 'res_test_robot_session_server', 'a')]
        class_paths = [os.path.join(parent_path, 'res_test_robot_session_server', 'b')]
        old_sys_path = sorted(sys.path)

        create_libdoc("lib", 'XML', python_paths, class_paths)

        self.assertEqual(old_sys_path, sorted(sys.path))


class ClassesRetrievingTests(unittest.TestCase):
    def test_subsequent_calls_for_same_class_name_under_different_paths_return_different_class_names(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))

        response1 = get_classes_from_module(os.path.join(parent_path, 'res_test_robot_session_server', 'a', 'LibClass.py'), [], [])
        response2 = get_classes_from_module(os.path.join(parent_path, 'res_test_robot_session_server', 'b', 'LibClass.py'), [], [])

        self.assertNotEqual(response1, response2)

    def test_if_sys_path_is_not_extended_after_retrieving_classes_from_python_module(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))

        python_paths = [os.path.join(parent_path, 'res_test_robot_session_server', 'a')]
        class_paths = [os.path.join(parent_path, 'res_test_robot_session_server', 'b')]
        old_sys_path = sorted(sys.path)

        get_classes_from_module(os.path.join(parent_path, 'res_test_robot_session_server', 'a', 'LibClass.py'), python_paths, class_paths)

        self.assertEqual(old_sys_path, sorted(sys.path))


class ModulePathRetrievingTests(unittest.TestCase):
    def test_if_sys_path_is_not_extended_after_retrieving_module_path(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))

        python_paths = [os.path.join(parent_path, 'res_test_robot_session_server', 'a')]
        class_paths = [os.path.join(parent_path, 'res_test_robot_session_server', 'b')]
        old_sys_path = sorted(sys.path)

        get_module_path("SomeModuleName", python_paths, class_paths)

        self.assertEqual(old_sys_path, sorted(sys.path))


class VariablesRetrievingTests(unittest.TestCase):
    def test_subsequent_calls_for_same_variable_file_name_under_different_paths_return_different_variables(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))

        response1 = get_variables(os.path.join(parent_path, 'res_test_robot_session_server', 'a', 'vars.py'), [])
        response2 = get_variables(os.path.join(parent_path, 'res_test_robot_session_server', 'b', 'vars.py'), [])

        self.assertNotEqual(response1, response2)
