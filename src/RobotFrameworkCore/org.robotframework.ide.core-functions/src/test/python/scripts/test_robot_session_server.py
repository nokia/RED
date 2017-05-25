import unittest
import sys
import os

from robot_session_server import create_libdoc
from robot_session_server import get_classes_from_module
from robot_session_server import get_variables


class LibdocGenerationTests(unittest.TestCase):
    def test_subsequent_lidocs_for_same_name_libs_under_different_paths_returns_different_libdocs(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))

        libdoc1 = create_libdoc("lib", [os.path.join(parent_path, 'res_test_robot_session_server', 'a')], [])
        libdoc2 = create_libdoc("lib", [os.path.join(parent_path, 'res_test_robot_session_server', 'b')], [])

        self.assertNotEqual(libdoc1, libdoc2)


class ClassesRetrievingTests(unittest.TestCase):
    def test_retrieving_classes_from_python_module_with_init(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))
        module_location = os.path.join(parent_path, 'res_test_robot_session_server', 'module', '__init__.py')

        classes = get_classes_from_module(module_location, None, [], [])

        self.assertEqual(classes,
                         {'result': ['module', 'mod_1', 'mod_2', 'module.module', 'module.mod_1', 'module.mod_2'],
                          'exception': None})

    def test_retrieving_classes_from_python_module_with_several_classes(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))
        module_location = os.path.join(parent_path, 'res_test_robot_session_server', 'module', 'mod_2.py')

        classes = get_classes_from_module(module_location, None, [], [])

        self.assertEqual(classes,
                         {'result': ['mod_2', 'mod_2.mod_2', 'mod_2.other_mod_2', 'module.mod_2', 'module.mod_2.mod_2',
                                     'module.mod_2.other_mod_2'], 'exception': None})

    def test_retrieving_classes_from_python_module_in_zip(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))
        module_location = os.path.join(parent_path, 'res_test_robot_session_server', 'module', 'compressed.zip')

        classes = get_classes_from_module(module_location, None, [], [])

        self.assertEqual(classes,
                         {'result': ['compressed.mod_compressed_1', 'compressed.mod_compressed_2'], 'exception': None})

    def test_retrieving_classes_from_python_module_in_jar(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))
        module_location = os.path.join(parent_path, 'res_test_robot_session_server', 'module', 'compressed.jar')

        classes = get_classes_from_module(module_location, None, [], [])

        self.assertEqual(classes,
                         {'result': ['compressed.mod_compressed_1', 'compressed.mod_compressed_2'], 'exception': None})

    def test_if_sys_path_is_not_extended_after_retrieving_classes_from_python_module(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))
        module_location = os.path.join(parent_path, 'res_test_robot_session_server', 'module', '__init__.py')
        python_paths = [os.path.join(parent_path, 'res_test_robot_session_server', 'a')]
        class_paths = [os.path.join(parent_path, 'res_test_robot_session_server', 'b')]
        old_sys_path = sorted(sys.path)

        get_classes_from_module(module_location, None, python_paths, class_paths)

        self.assertListEqual(old_sys_path, sorted(sys.path))


class VariablesRetrievingTests(unittest.TestCase):
    def test_if_empty_result_is_returned_for_empty_file(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))

        vars = get_variables(os.path.join(parent_path, 'res_test_robot_session_server', 'variables', 'empty.py'), [])

        self.assertEqual(vars, {'result': {}, 'exception': None})

    def test_if_result_is_returned_for_vars_in_lines(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))

        vars = get_variables(os.path.join(parent_path, 'res_test_robot_session_server', 'variables', 'vars_in_lines.py'), [])

        self.assertEqual(vars, {'result': {'first': '123', 'second': '234', 'third': '345'}, 'exception': None})

    def test_if_result_is_returned_for_vars_in_method(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))

        vars = get_variables(os.path.join(parent_path, 'res_test_robot_session_server', 'variables', 'vars_in_method.py'), [])

        self.assertEqual(vars, {'result': {'a': '1', 'b': '2', 'c': '3'}, 'exception': None})

    def test_if_result_is_returned_for_vars_in_class(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))

        vars = get_variables(os.path.join(parent_path, 'res_test_robot_session_server', 'variables', 'vars_in_class.py'), [])

        self.assertEqual(vars, {'result': {'x': '9', 'y': '8', 'z': '7'}, 'exception': None})

    def test_if_data_error_is_returned_for_not_existing_file(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))

        vars = get_variables(os.path.join(parent_path, 'res_test_robot_session_server', 'variables', 'not_existing.py'), [])

        self.assertEqual(vars['result'], None)
        self.assertTrue('robot.errors.DataError: ' in vars['exception'])

    def test_if_syntax_error_is_returned_for_file_with_error(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))

        vars = get_variables(os.path.join(parent_path, 'res_test_robot_session_server', 'variables', 'vars_with_syntax_error.py'), [])

        self.assertEqual(vars['result'], None)
        self.assertTrue('SyntaxError: ' in vars['exception'])
