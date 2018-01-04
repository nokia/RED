import unittest
import os

from red_module_classes import get_classes_from_module


class ClassesRetrievingTests(unittest.TestCase):
    def test_retrieving_classes_from_file_with_error(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))
        module_location = os.path.join(parent_path, 'res_test_red_module_classes', 'error.py')

        with self.assertRaises(SyntaxError) as cm:
            get_classes_from_module(module_location)

    def test_retrieving_classes_from_file_with_cycle(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))
        module_location = os.path.join(parent_path, 'res_test_red_module_classes', 'cycle', 'module_a.py')

        with self.assertRaises(ImportError) as cm:
            get_classes_from_module(module_location)

    def test_retrieving_classes_from_empty_file(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))
        module_location = os.path.join(parent_path, 'res_test_red_module_classes', 'empty.py')

        result = get_classes_from_module(module_location)

        self.assertEqual(result, [])

    def test_retrieving_classes_from_file_with_same_class_name(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))
        module_location = os.path.join(parent_path, 'res_test_red_module_classes', 'ClassName.py')

        result = get_classes_from_module(module_location)

        self.assertEqual(result, ['ClassName'])

    def test_retrieving_classes_from_file_with_different_class_name(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))
        module_location = os.path.join(parent_path, 'res_test_red_module_classes', 'DifferentClassName.py')

        result = get_classes_from_module(module_location)

        self.assertEqual(result, ['DifferentClassName.ClassName'])

    def test_retrieving_classes_from_file_with_several_classes(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))
        module_location = os.path.join(parent_path, 'res_test_red_module_classes', 'several_classes.py')

        result = get_classes_from_module(module_location)

        self.assertEqual(result, ['several_classes.First', 'several_classes.Second', 'several_classes.Third'])

    def test_retrieving_classes_from_file_with_several_classes_and_methods(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))
        module_location = os.path.join(parent_path, 'res_test_red_module_classes', 'several_classes_and_methods.py')

        result = get_classes_from_module(module_location)

        self.assertEqual(result, ['several_classes_and_methods', 'several_classes_and_methods.First', 'several_classes_and_methods.Second',
                                  'several_classes_and_methods.Third'])

    def test_retrieving_classes_from_file_with_specyfing_module_name(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))
        module_location = os.path.join(parent_path, 'res_test_red_module_classes', 'ClassName.py')

        result = get_classes_from_module(module_location, 'ClassName.ClassName')

        self.assertEqual(result, ['ClassName', 'ClassName.ClassName'])

    def test_retrieving_classes_from_python_module_with_init_1(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))
        module_location = os.path.join(parent_path, 'res_test_red_module_classes', 'init', '__init__.py')

        result = get_classes_from_module(module_location)

        self.assertEqual(result, ['init', 'init.InitClass', 'init.OtherInitClass'])

    def test_retrieving_classes_from_python_module_with_init_2(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))
        module_location = os.path.join(parent_path, 'res_test_red_module_classes', 'module', '__init__.py')

        result = get_classes_from_module(module_location)

        self.assertEqual(result, ['ModuleClass', 'OtherModuleClass', 'module', 'module.ModuleClass', 'module.OtherModuleClass'])

    def test_retrieving_classes_from_python_module_with_init_3(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))
        module_location = os.path.join(parent_path, 'res_test_red_module_classes', 'init_and_module', '__init__.py')

        result = get_classes_from_module(module_location)

        self.assertEqual(result, ['ModuleClass', 'OtherModuleClass', 'init_and_module', 'init_and_module.InitClass',
                                  'init_and_module.ModuleClass', 'init_and_module.OtherInitClass', 'init_and_module.OtherModuleClass'])

    def test_retrieving_classes_from_python_module_in_zip(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))
        module_location = os.path.join(parent_path, 'res_test_red_module_classes', 'compressed.zip')

        result = get_classes_from_module(module_location)

        self.assertEqual(result, ['compressed', 'compressed.mod_compressed_1', 'compressed.mod_compressed_2'])

    def test_retrieving_classes_from_python_module_in_jar(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))
        module_location = os.path.join(parent_path, 'res_test_red_module_classes', 'compressed.jar')

        result = get_classes_from_module(module_location)

        self.assertEqual(result, ['compressed', 'compressed.mod_compressed_1', 'compressed.mod_compressed_2'])
