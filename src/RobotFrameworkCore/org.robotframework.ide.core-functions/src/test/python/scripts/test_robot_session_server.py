# -*- coding: utf-8 -*-
import unittest
import sys
import os
import platform

from robot_session_server import get_robot_version
from robot_session_server import create_libdoc
from robot_session_server import create_libdoc_in_separate_process
from robot_session_server import get_classes_from_module
from robot_session_server import get_module_path
from robot_session_server import get_variables
from robot_session_server import get_standard_library_path
from robot_session_server import convert_robot_data_file
from base64 import b64encode


class RobotSessionServerTests(unittest.TestCase):

    def test_encode_result(self):
        response = get_robot_version()

        self.assertTrue(response['result'].startswith('Robot Framework '))
        self.assertEqual(response['exception'], None)

    def test_encode_exception(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))

        response = get_standard_library_path("NotExistingLib")

        self.assertEqual(response['result'], None)
        if sys.version_info < (3, 0, 0):
            self.assertTrue('ImportError: ' in response['exception'])
        else:
            self.assertTrue('ModuleNotFoundError: ' in response['exception'])


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
        
    def test_if_libdoc_is_not_generated_and_exception_is_thrown_for_library_with_error(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))

        response = create_libdoc("LibError", 'XML', [os.path.join(parent_path, 'res_test_robot_session_server')], [])

        self.assertEqual(response['result'], None)
        self.assertTrue('SyntaxError: ' in response['exception'])
        
    def test_if_libdoc_is_not_generated_in_separate_process_and_exception_is_thrown_for_library_with_error(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))
        timeout_duration = 5 if 'Jython' not in platform.python_implementation() else 20

        response = create_libdoc_in_separate_process("LibError", 'XML', [os.path.join(parent_path, 'res_test_robot_session_server')], [], timeout_duration)

        self.assertEqual(response['result'], None)
        self.assertTrue('SyntaxError: ' in response['exception'])

    def test_if_libdoc_is_generated_in_separate_process_for_existing_library(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))
        python_paths = [os.path.join(parent_path, 'res_test_robot_session_server', 'a')]
        timeout_duration = 5 if 'Jython' not in platform.python_implementation() else 20

        response = create_libdoc_in_separate_process("lib", 'XML', python_paths, [], timeout_duration)

        self.assertNotEqual(response["result"], None)

    def test_if_libdoc_is_not_generated_in_separate_process_for_hanging_module_importing(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))
        python_paths = [os.path.join(parent_path, 'res_test_robot_session_server')]
        timeout_duration = 5 if 'Jython' not in platform.python_implementation() else 20

        response = create_libdoc_in_separate_process("c.HangingInit", 'XML', python_paths, [], timeout_duration)

        self.assertTrue('Libdoc not generated due to timeout' in response['exception'])
        
    def test_if_libdoc_is_generated_in_separate_process_for_library_with_logger(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))
        python_paths = [os.path.join(parent_path, 'res_test_robot_session_server', 'a')]
        timeout_duration = 5 if 'Jython' not in platform.python_implementation() else 20

        response = create_libdoc_in_separate_process("lib_logger", 'XML', python_paths, [], timeout_duration)

        self.assertNotEqual(response["result"], None)


@unittest.skipUnless(platform.python_version_tuple()[0] == '2', "requires Python 2")
class LibdocGenerationForPython2Tests(unittest.TestCase):

    def test_if_libdoc_is_not_generated_in_separate_process_for_non_ascii_chars_in_library_path(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))
        timeout_duration = 5 if 'Jython' not in platform.python_implementation() else 20
        
        response = create_libdoc_in_separate_process("żółw", 'XML', [os.path.join(parent_path, 'res_test_robot_session_server', 'żółw')], [], timeout_duration)

        self.assertTrue('UnicodeDecodeError', response['exception'])

    def test_if_libdoc_is_not_generated_for_non_ascii_chars_in_library_path(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))
        timeout_duration = 5 if 'Jython' not in platform.python_implementation() else 20
        
        response = create_libdoc("żółw", 'XML', [os.path.join(parent_path, 'res_test_robot_session_server', 'żółw')], [], timeout_duration)

        self.assertTrue('UnicodeDecodeError', response['exception'])


@unittest.skipUnless(platform.python_version_tuple()[0] == '3', "requires Python 3")
class LibdocGenerationForPython3Tests(unittest.TestCase):
    
    def test_if_libdoc_is_not_generated_in_separate_process_for_non_ascii_chars_in_error_library_path(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))

        response = create_libdoc_in_separate_process("LibError", 'XML', [os.path.join(parent_path, 'res_test_robot_session_server', 'żółw')], [])

        self.assertEqual(response['result'], None)
        self.assertTrue('SyntaxError: ' in response['exception'])

    def test_if_libdoc_is_generated_in_separate_process_for_non_ascii_chars_in_library_path(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))

        response = create_libdoc_in_separate_process("lib", 'XML', [os.path.join(parent_path, 'res_test_robot_session_server', 'żółw')], [])

        self.assertNotEqual(response['result'], None)

    def test_if_libdoc_is_generated_for_non_ascii_chars_in_library_path(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))

        response = create_libdoc("lib", 'XML', [os.path.join(parent_path, 'res_test_robot_session_server', 'żółw')], [])

        self.assertNotEqual(response['result'], None)


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

        response1 = get_variables(os.path.join(parent_path, 'res_test_robot_session_server', 'a', 'vars.py'), [], [])
        response2 = get_variables(os.path.join(parent_path, 'res_test_robot_session_server', 'b', 'vars.py'), [], [])

        self.assertNotEqual(response1, response2)
        
    def test_variables_are_not_returned_for_variable_file_importing_module_not_visible_in_pythonpath(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))

        response = get_variables(os.path.join(parent_path, 'res_test_robot_session_server', 'vars_modules', 'a', 'b.py'), [], [])
        
        self.assertIn('Traceback', response['exception'])
        self.assertTrue(response['result'] is None)
        
    def test_variables_are_returned_for_variable_file_importing_module_visible_in_pythonpath(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))
        additional_path = os.path.join(parent_path, 'res_test_robot_session_server', 'vars_modules')

        response = get_variables(os.path.join(parent_path, 'res_test_robot_session_server', 'vars_modules', 'a', 'b.py'), [], [additional_path])
        
        self.assertTrue(response['exception'] is None)
        self.assertDictEqual(response['result'], {'x':'1', 'y':'2', 'z':'3'})


class RobotFilesConvertingTests(unittest.TestCase):

    def test_txt_file_is_properly_converted_to_robot_format(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))
        path = os.path.join(parent_path, 'res_test_robot_session_server', 'to_convert.txt')

        from base64 import b64decode
        converted = b64decode(convert_robot_data_file(path)['result'])

        golden_file_path = os.path.join(parent_path, 'res_test_robot_session_server', 'converted.robot')
        self.assertEqualToGoldenFileContent(converted, golden_file_path)

    def test_tsv_file_is_properly_converted_to_robot_format(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))
        path = os.path.join(parent_path, 'res_test_robot_session_server', 'to_convert.tsv')

        from base64 import b64decode
        converted = b64decode(convert_robot_data_file(path)['result'])

        golden_file_path = os.path.join(parent_path, 'res_test_robot_session_server', 'converted.robot')
        self.assertEqualToGoldenFileContent(converted, golden_file_path)

    def test_file_with_unicode_charactes_is_converted_to_robot_format_without_exceptions(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))
        path = os.path.join(parent_path, 'res_test_robot_session_server', 'to_convert_unicode.tsv')

        from base64 import b64decode
        b64decode(convert_robot_data_file(path)['result'])

    def assertEqualToGoldenFileContent(self, content, golden_file_path):
        if sys.version_info < (3, 0, 0):
            with open(golden_file_path, 'r') as golden_file:
                content_in_lines = content.splitlines()
                golden_file_lines = golden_file.readlines()
        else:
            with open(golden_file_path, 'r', encoding='utf-8') as golden_file:
                content_in_lines = content.decode('utf-8').splitlines()
                golden_file_lines = golden_file.readlines()

        self.assertEqual(len(content_in_lines), len(golden_file_lines))

        for i in range(len(content_in_lines)):
            self.assertEqual(content_in_lines[i], golden_file_lines[i].rstrip())
