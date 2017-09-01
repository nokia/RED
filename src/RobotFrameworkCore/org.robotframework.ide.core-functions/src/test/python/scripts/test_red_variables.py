import unittest
import os

from red_variables import get_variables
from robot.errors import DataError


class ClassesRetrievingTests(unittest.TestCase):
    def test_if_empty_result_is_returned_for_empty_file(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))
        vars_location = os.path.join(parent_path, 'res_test_robot_session_server', 'variables', 'empty.py')

        result = get_variables(vars_location, [])

        self.assertEqual(result, {})

    def test_if_result_is_returned_for_vars_in_lines(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))
        vars_location = os.path.join(parent_path, 'res_test_robot_session_server', 'variables', 'vars_in_lines.py')

        result = get_variables(vars_location, [])

        self.assertEqual(result, {'first': '123', 'second': '234', 'third': '345'})

    def test_if_result_is_returned_for_vars_in_method(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))
        vars_location = os.path.join(parent_path, 'res_test_robot_session_server', 'variables', 'vars_in_method.py')

        result = get_variables(vars_location, [])

        self.assertEqual(result, {'a': '1', 'b': '2', 'c': '3'})

    def test_if_result_is_returned_for_vars_in_class(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))
        vars_location = os.path.join(parent_path, 'res_test_robot_session_server', 'variables', 'vars_in_class.py')

        result = get_variables(vars_location, [])

        self.assertEqual(result, {'x': '9', 'y': '8', 'z': '7'})

    def test_if_result_is_returned_for_vars_with_argument(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))
        vars_location = os.path.join(parent_path, 'res_test_robot_session_server', 'variables', 'vars_with_argument.py')

        result = get_variables(vars_location, ['_arg'])

        self.assertEqual(result, {'a': '1_arg', 'b': '2_arg', 'c': '3_arg'})

    def test_if_data_error_is_raised_for_file_without_arguments(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))
        vars_location = os.path.join(parent_path, 'res_test_robot_session_server', 'variables', 'vars_with_argument.py')

        with self.assertRaises(DataError) as cm:
            get_variables(vars_location, [])

        self.assertTrue('TypeError: ' in cm.exception.message)

    def test_if_data_error_is_raised_for_not_existing_file(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))
        vars_location = os.path.join(parent_path, 'res_test_robot_session_server', 'variables', 'not_existing.py')

        with self.assertRaises(DataError) as cm:
            get_variables(vars_location, [])

        self.assertTrue('File or directory does not exist' in cm.exception.message)

    def test_if_data_error_is_raised_for_file_with_unsupported_extension(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))
        vars_location = os.path.join(parent_path, 'res_test_robot_session_server', 'variables', 'vars_in_unsupported.robot')

        with self.assertRaises(DataError) as cm:
            get_variables(vars_location, [])

        self.assertTrue('Not a valid file or directory to import' in cm.exception.message)

    def test_if_data_error_is_raised_for_file_with_error(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))
        vars_location = os.path.join(parent_path, 'res_test_robot_session_server', 'variables', 'vars_with_syntax.py')

        with self.assertRaises(DataError) as cm:
            get_variables(vars_location, [])

        self.assertTrue('SyntaxError: ' in cm.exception.message)
