import unittest
import os

from red_module_classes import get_classes_from_module


class ClassesRetrievingTests(unittest.TestCase):
    def test_retrieving_classes_from_empty_file(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))
        module_location = os.path.join(parent_path, 'res_test_robot_session_server', 'classes', 'empty.py')

        result = get_classes_from_module(module_location, None)

        self.assertEqual(result, ['empty'])
        
    def test_retrieving_classes_from_file_with_same_class_name(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))
        module_location = os.path.join(parent_path, 'res_test_robot_session_server', 'classes', 'ClassName.py')

        result = get_classes_from_module(module_location, None)

        self.assertEqual(result, ['ClassName', 'ClassName.ClassName'])
        
    def test_retrieving_classes_from_file_with_different_class_name(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))
        module_location = os.path.join(parent_path, 'res_test_robot_session_server', 'classes', 'DifferentClassName.py')

        result = get_classes_from_module(module_location, None)

        self.assertEqual(result, ['DifferentClassName', 'DifferentClassName.ClassName'])
        
    def test_retrieving_classes_from_file_with_several_classes(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))
        module_location = os.path.join(parent_path, 'res_test_robot_session_server', 'classes', 'several_classes.py')

        result = get_classes_from_module(module_location, None)

        self.assertEqual(result, ['several_classes', 'several_classes.First', 'several_classes.Second', 'several_classes.Third'])
        
    def test_retrieving_classes_from_python_module_with_init_1(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))
        module_location = os.path.join(parent_path, 'res_test_robot_session_server', 'classes', 'init', '__init__.py')

        result = get_classes_from_module(module_location, None)

        self.assertEqual(result, ['init', 'init.InitClass', 'init.OtherInitClass'])
        
    def test_retrieving_classes_from_python_module_with_init_2(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))
        module_location = os.path.join(parent_path, 'res_test_robot_session_server', 'classes', 'module', '__init__.py')

        result = get_classes_from_module(module_location, None)

        self.assertEqual(result, ['module', 'ModuleClass', 'OtherModuleClass', 'module.ModuleClass', 'module.OtherModuleClass'])
        
    def test_retrieving_classes_from_python_module_with_init_3(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))
        module_location = os.path.join(parent_path, 'res_test_robot_session_server', 'classes', 'init_and_module', '__init__.py')

        result = get_classes_from_module(module_location, None)

        self.assertEqual(result, ['init_and_module', 'init_and_module.InitClass', 'init_and_module.OtherInitClass', 'ModuleClass', 'OtherModuleClass', 'init_and_module.ModuleClass', 'init_and_module.OtherModuleClass'])

    def test_retrieving_classes_from_python_module_in_zip(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))
        module_location = os.path.join(parent_path, 'res_test_robot_session_server', 'classes', 'compressed.zip')

        result = get_classes_from_module(module_location, None)

        self.assertEqual(result, ['compressed.mod_compressed_1', 'compressed.mod_compressed_2'])

    def test_retrieving_classes_from_python_module_in_jar(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))
        module_location = os.path.join(parent_path, 'res_test_robot_session_server', 'classes', 'compressed.jar')

        result = get_classes_from_module(module_location, None)

        self.assertEqual(result, ['compressed.mod_compressed_1', 'compressed.mod_compressed_2'])
