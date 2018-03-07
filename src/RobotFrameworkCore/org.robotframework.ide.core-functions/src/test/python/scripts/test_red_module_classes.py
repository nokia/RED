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

        self.assertEqual(result, ['empty'])

    def test_retrieving_classes_from_file_with_same_class_name(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))
        module_location = os.path.join(parent_path, 'res_test_red_module_classes', 'ClassName.py')

        result = get_classes_from_module(module_location)

        self.assertEqual(result, ['ClassName', 'ClassName.ClassName'])

    def test_retrieving_classes_from_file_with_different_class_name(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))
        module_location = os.path.join(parent_path, 'res_test_red_module_classes', 'DifferentClassName.py')

        result = get_classes_from_module(module_location)

        self.assertEqual(result, ['DifferentClassName', 'DifferentClassName.ClassName'])

    def test_retrieving_classes_from_file_with_several_classes(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))
        module_location = os.path.join(parent_path, 'res_test_red_module_classes', 'several_classes.py')

        result = get_classes_from_module(module_location)

        self.assertEqual(result, ['several_classes', 'several_classes.First', 'several_classes.Second', 'several_classes.Third'])

    def test_retrieving_classes_from_file_with_several_classes_and_methods(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))
        module_location = os.path.join(parent_path, 'res_test_red_module_classes', 'several_classes_and_methods.py')

        result = get_classes_from_module(module_location)

        self.assertEqual(result, ['several_classes_and_methods', 'several_classes_and_methods.First', 'several_classes_and_methods.Second',
                                  'several_classes_and_methods.Third'])

    def test_retrieving_classes_from_file_with_same_name_inside_module(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))
        module_location = os.path.join(parent_path, 'res_test_red_module_classes', 'module_diff_names', 'SameModuleClassName.py')

        result = get_classes_from_module(module_location)

        self.assertEqual(result, ['module_diff_names.SameModuleClassName', 'module_diff_names.SameModuleClassName.SameModuleClassName'])

    def test_retrieving_classes_from_file_with_different_name_inside_module(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))
        module_location = os.path.join(parent_path, 'res_test_red_module_classes', 'module_diff_names', 'DifferentModuleClassName.py')

        result = get_classes_from_module(module_location)

        self.assertEqual(result, ['module_diff_names.DifferentModuleClassName', 'module_diff_names.DifferentModuleClassName.OtherClassName'])

    def test_retrieving_classes_from_file_in_directory_with_same_name_like_sys_module(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))
        module_location = os.path.join(parent_path, 'res_test_red_module_classes', 'robot', 'CustomRobotClassName.py')

        result = get_classes_from_module(module_location)

        self.assertEqual(result, ['CustomRobotClassName', 'CustomRobotClassName.CustomRobotClassName'])

    def test_retrieving_classes_from_module_with_relative_iports(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))
        module_location = os.path.join(parent_path, 'res_test_red_module_classes', 'relative_import', '__init__.py')

        result = get_classes_from_module(module_location)

        self.assertEqual(result, ['relative_import', 'relative_import.non_relative'])

    def test_retrieving_classes_from_python_module_with_init_only(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))
        module_location = os.path.join(parent_path, 'res_test_red_module_classes', 'init', '__init__.py')

        result = get_classes_from_module(module_location)

        self.assertEqual(result, ['init', 'init.InitClass', 'init.OtherInitClass'])

    def test_retrieving_classes_from_python_module_with_empty_init_and_classes(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))
        module_location = os.path.join(parent_path, 'res_test_red_module_classes', 'module', '__init__.py')

        result = get_classes_from_module(module_location)

        self.assertEqual(result, ['module', 'module.ModuleClass', 'module.ModuleClass.ModuleClass', 'module.OtherModuleClass',
                                  'module.OtherModuleClass.DifferentModuleClass', 'module.OtherModuleClass.OtherModuleClass'])

    def test_retrieving_classes_from_python_module_with_init_and_classes(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))
        module_location = os.path.join(parent_path, 'res_test_red_module_classes', 'init_and_module', '__init__.py')

        result = get_classes_from_module(module_location)

        self.assertEqual(result, ['init_and_module', 'init_and_module.InitClass', 'init_and_module.ModuleClass',
                                  'init_and_module.ModuleClass.ModuleClass', 'init_and_module.OtherInitClass',
                                  'init_and_module.OtherModuleClass', 'init_and_module.OtherModuleClass.DifferentModuleClass',
                                  'init_and_module.OtherModuleClass.OtherModuleClass'])

    def test_retrieving_classes_from_python_module_in_zip(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))
        module_location = os.path.join(parent_path, 'res_test_red_module_classes', 'JythonLibWithPython.zip')

        result = get_classes_from_module(module_location)

        import platform
        if 'Jython' in platform.python_implementation():
            self.assertEqual(result, ['PythonOnly', 'PythonOnly.Other', 'PythonWithJava'])
        else:
            self.assertEqual(result, ['PythonOnly', 'PythonOnly.Other'])

    def test_retrieving_classes_from_python_module_in_jar(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))
        module_location = os.path.join(parent_path, 'res_test_red_module_classes', 'JythonLibWithPython.jar')

        result = get_classes_from_module(module_location)

        import platform
        if 'Jython' in platform.python_implementation():
            self.assertEqual(result, ['PythonOnly', 'PythonOnly.Other', 'PythonWithJava'])
        else:
            self.assertEqual(result, ['PythonOnly', 'PythonOnly.Other'])
