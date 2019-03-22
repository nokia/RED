import unittest
import os

from red_module_classes import get_classes_from_module, isJarOrZip


class ClassesRetrievingTests(unittest.TestCase):
    def test_retrieving_classes_from_file_with_error(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))
        module_location = os.path.join(parent_path, 'res_test_red_module_classes', 'error.py')

        with self.assertRaises(SyntaxError) as cm:
            get_classes_from_module(module_location)

    def test_retrieving_classes_from_file_with_cycle(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))
        module_location = os.path.join(parent_path, 'res_test_red_module_classes', 'cycle', 'module_a.py')

        import platform
        if 'Jython' in platform.python_implementation():
            with self.assertRaises(ImportError) as cm:
                get_classes_from_module(module_location)
        else:
            result = get_classes_from_module(module_location)
            self.assertEqual(result, ['module_a'])

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

        self.assertEqual(result, ['several_classes', 'several_classes.First',
                                  'several_classes.Second', 'several_classes.Third'])

    def test_retrieving_classes_from_file_with_several_classes_and_methods(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))
        module_location = os.path.join(parent_path, 'res_test_red_module_classes', 'several_classes_and_methods.py')

        result = get_classes_from_module(module_location)

        self.assertEqual(result, ['several_classes_and_methods', 'several_classes_and_methods.First', 'several_classes_and_methods.Second',
                                  'several_classes_and_methods.Third'])

    def test_retrieving_classes_from_file_with_several_classes_and_comments(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))
        module_location = os.path.join(parent_path, 'res_test_red_module_classes', 'classes_with_comments.py')

        result = get_classes_from_module(module_location)

        self.assertEqual(result, ['classes_with_comments', 'classes_with_comments.Cat', 'classes_with_comments.Dog'])

    def test_retrieving_classes_from_file_with_several_methods_and_documentation(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))
        module_location = os.path.join(parent_path, 'res_test_red_module_classes', 'docs_and_methods.py')

        result = get_classes_from_module(module_location)

        self.assertEqual(result, ['docs_and_methods'])

    def test_retrieving_classes_from_file_with_same_name_inside_module(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))
        module_location = os.path.join(parent_path, 'res_test_red_module_classes', 'module_diff_names', 'SameModuleClassName.py')

        result = get_classes_from_module(module_location)

        self.assertEqual(result, ['SameModuleClassName', 'SameModuleClassName.SameModuleClassName', 'module_diff_names.SameModuleClassName',
                                  'module_diff_names.SameModuleClassName.SameModuleClassName'])

    def test_retrieving_classes_from_file_with_different_name_inside_module(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))
        module_location = os.path.join(parent_path, 'res_test_red_module_classes', 'module_diff_names', 'DifferentModuleClassName.py')

        result = get_classes_from_module(module_location)

        self.assertEqual(result, ['DifferentModuleClassName', 'DifferentModuleClassName.OtherClassName',
                                  'module_diff_names.DifferentModuleClassName', 'module_diff_names.DifferentModuleClassName.OtherClassName'])

    def test_retrieving_classes_from_file_in_directory_with_same_name_like_sys_module(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))
        module_location = os.path.join(parent_path, 'res_test_red_module_classes', 'robot', 'CustomRobotClassName.py')

        result = get_classes_from_module(module_location)

        self.assertEqual(result, ['CustomRobotClassName', 'CustomRobotClassName.CustomRobotClassName', 'robot.CustomRobotClassName',
                                  'robot.CustomRobotClassName.CustomRobotClassName'])

    def test_retrieving_classes_from_module_with_relative_imports(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))
        module_location = os.path.join(parent_path, 'res_test_red_module_classes', 'relative_import', '__init__.py')

        result = get_classes_from_module(module_location)
        
        import platform
        if 'Jython' in platform.python_implementation():
            self.assertEqual(result, ['relative_import', 'relative_import.non_relative'])
        else:
            self.assertEqual(result, ['relative_import', 'relative_import.non_relative', 'relative_import.relative', 'relative_import.relative.Relative'])

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

    def test_retrieving_classes_from_nested_python_module_1(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))
        module_location = os.path.join(parent_path, 'res_test_red_module_classes', 'mod_outer', '__init__.py')

        result = get_classes_from_module(module_location)

        import platform
        if 'Jython' in platform.python_implementation():
            self.assertEqual(result, ['mod_outer', 'mod_outer.ClassA', 'mod_outer.ClassB', 'mod_outer.OtherClassA',
                                      'mod_outer.mod_a', 'mod_outer.mod_b', 'mod_outer.mod_inner', 'mod_outer.mod_inner.mod_a',
                                      'mod_outer.mod_inner.mod_a.ClassA', 'mod_outer.mod_inner.mod_a.ClassA.ClassA',
                                      'mod_outer.mod_inner.mod_a.OtherClassA', 'mod_outer.mod_inner.mod_a.OtherClassA.OtherClassA',
                                      'mod_outer.mod_inner.mod_a.mod_a', 'mod_outer.mod_inner.mod_b', 'mod_outer.mod_inner.mod_b.ClassB',
                                      'mod_outer.mod_inner.mod_b.ClassB.ClassB', 'mod_outer.mod_inner.mod_b.mod_b'])
        else:
            self.assertEqual(result, ['mod_outer', 'mod_outer.mod_inner', 'mod_outer.mod_inner.mod_a',
                                      'mod_outer.mod_inner.mod_a.ClassA', 'mod_outer.mod_inner.mod_a.ClassA.ClassA',
                                      'mod_outer.mod_inner.mod_a.OtherClassA', 'mod_outer.mod_inner.mod_a.OtherClassA.OtherClassA',
                                      'mod_outer.mod_inner.mod_b', 'mod_outer.mod_inner.mod_b.ClassB',
                                      'mod_outer.mod_inner.mod_b.ClassB.ClassB'])

    def test_retrieving_classes_from_nested_python_module_2(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))
        module_location = os.path.join(parent_path, 'res_test_red_module_classes', 'mod_outer', 'mod_inner', '__init__.py')

        result = get_classes_from_module(module_location)

        import platform
        if 'Jython' in platform.python_implementation():
            self.assertEqual(result, ['mod_inner', 'mod_inner.ClassA', 'mod_inner.ClassB', 'mod_inner.OtherClassA', 'mod_inner.mod_a',
                                      'mod_inner.mod_a.ClassA', 'mod_inner.mod_a.ClassA.ClassA', 'mod_inner.mod_a.OtherClassA',
                                      'mod_inner.mod_a.OtherClassA.OtherClassA', 'mod_inner.mod_a.mod_a', 'mod_inner.mod_b',
                                      'mod_inner.mod_b.ClassB', 'mod_inner.mod_b.ClassB.ClassB', 'mod_inner.mod_b.mod_b', 'mod_outer.mod_inner',
                                      'mod_outer.mod_inner.ClassA', 'mod_outer.mod_inner.ClassB', 'mod_outer.mod_inner.OtherClassA',
                                      'mod_outer.mod_inner.mod_a', 'mod_outer.mod_inner.mod_a.ClassA', 'mod_outer.mod_inner.mod_a.ClassA.ClassA',
                                      'mod_outer.mod_inner.mod_a.OtherClassA', 'mod_outer.mod_inner.mod_a.OtherClassA.OtherClassA',
                                      'mod_outer.mod_inner.mod_a.mod_a', 'mod_outer.mod_inner.mod_b', 'mod_outer.mod_inner.mod_b.ClassB',
                                      'mod_outer.mod_inner.mod_b.ClassB.ClassB', 'mod_outer.mod_inner.mod_b.mod_b'])
        else:
            self.assertEqual(result, ['mod_inner', 'mod_inner.mod_a',
                                      'mod_inner.mod_a.ClassA', 'mod_inner.mod_a.ClassA.ClassA',
                                      'mod_inner.mod_a.OtherClassA', 'mod_inner.mod_a.OtherClassA.OtherClassA',
                                      'mod_inner.mod_b', 'mod_inner.mod_b.ClassB',
                                      'mod_inner.mod_b.ClassB.ClassB', 'mod_outer.mod_inner', 'mod_outer.mod_inner.mod_a',
                                      'mod_outer.mod_inner.mod_a.ClassA', 'mod_outer.mod_inner.mod_a.ClassA.ClassA',
                                      'mod_outer.mod_inner.mod_a.OtherClassA', 'mod_outer.mod_inner.mod_a.OtherClassA.OtherClassA',
                                      'mod_outer.mod_inner.mod_b', 'mod_outer.mod_inner.mod_b.ClassB',
                                      'mod_outer.mod_inner.mod_b.ClassB.ClassB'])

    def test_retrieving_classes_from_nested_python_module_3(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))
        module_location = os.path.join(parent_path, 'res_test_red_module_classes', 'mod_outer', 'mod_inner', 'mod_a', '__init__.py')

        result = get_classes_from_module(module_location)

        self.assertEqual(result, ['mod_a', 'mod_a.ClassA', 'mod_a.ClassA.ClassA', 'mod_a.OtherClassA', 'mod_a.OtherClassA.OtherClassA',
                                  'mod_a.mod_a', 'mod_inner.mod_a', 'mod_inner.mod_a.ClassA', 'mod_inner.mod_a.ClassA.ClassA',
                                  'mod_inner.mod_a.OtherClassA', 'mod_inner.mod_a.OtherClassA.OtherClassA', 'mod_inner.mod_a.mod_a',
                                  'mod_outer.mod_inner.mod_a', 'mod_outer.mod_inner.mod_a.ClassA', 'mod_outer.mod_inner.mod_a.ClassA.ClassA',
                                  'mod_outer.mod_inner.mod_a.OtherClassA', 'mod_outer.mod_inner.mod_a.OtherClassA.OtherClassA',
                                  'mod_outer.mod_inner.mod_a.mod_a'])
        

    def test_retrieving_classes_from_module_with_non_module_files(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))
        module_location = os.path.join(parent_path, 'res_test_red_module_classes', 'module_with_non_module_files', '__init__.py')

        result = get_classes_from_module(module_location)

        self.assertEqual(result, ['module_with_non_module_files', 'module_with_non_module_files.InitClass'])

    import sys
    @unittest.skipUnless(sys.version_info >= (3, 0, 0), "requires Python 3")
    def test_retrieving_classes_from_unicode_named_module(self):
        #For python2 unicode would not work properly because of its internal problems
        parent_path = os.path.dirname(os.path.realpath(__file__))
        module_location = os.path.join(parent_path, 'res_test_red_module_classes', 'UnicodeClass.py')

        result = get_classes_from_module(module_location)

        self.assertEqual(result, ['UnicodeClass', 'UnicodeClass.UnicodeClass'])
        

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
            
            
class ModuleLocationTest(unittest.TestCase):
    
    def test_if_module_location_is_jar_file(self):
        module_location = os.path.join('path', 'to', 'JarFile.jar')

        self.assertTrue(isJarOrZip(module_location))
        
    def test_if_module_location_is_zip_file(self):
        module_location = os.path.join('path', 'to', 'ZipFile.zip')

        self.assertTrue(isJarOrZip(module_location))
            
    def test_if_module_location_is_capital_jar_file(self):
        module_location = os.path.join('path', 'to', 'CapitalJarFile.JAR')

        self.assertTrue(isJarOrZip(module_location))
        
    def test_if_module_location_is_capital_zip_file(self):
        module_location = os.path.join('path', 'to', 'CapitalZipFile.ZIP')

        self.assertTrue(isJarOrZip(module_location))
        
    def test_if_module_location_is_not_jar_or_zip_file(self):
        module_location = os.path.join('path', 'to', 'PythonFile.py')

        self.assertFalse(isJarOrZip(module_location))
