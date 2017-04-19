import unittest
import sys
import os

from robot_session_server import _create_libdoc
from robot_session_server import _get_classes_from_module

class LibdocGenerationTests(unittest.TestCase):
    
    def test_subsequent_lidocs_for_same_name_libs_under_different_paths_returns_different_libdocs(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))
        
        libdoc1 = _create_libdoc("lib", [os.path.join(parent_path, 'res_test_robot_session_server', 'a')], [])
        libdoc2 = _create_libdoc("lib", [os.path.join(parent_path, 'res_test_robot_session_server', 'b')], [])
        
        self.assertNotEqual(libdoc1, libdoc2)
    
    
    def test_retrieving_classes_from_python_module_with_init(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))
        module_location = os.path.join(parent_path, 'res_test_robot_session_server', 'module', '__init__.py')
        
        classes = _get_classes_from_module(module_location, None, [], [])

        self.assertListEqual(classes, ['module', 'mod_1', 'mod_2', 'module.module', 'module.mod_1', 'module.mod_2'])

   
    def test_retrieving_classes_from_python_module_with_several_classes(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))
        module_location = os.path.join(parent_path, 'res_test_robot_session_server', 'module', 'mod_2.py')
        
        classes = _get_classes_from_module(module_location, None, [], [])

        self.assertListEqual(classes, ['mod_2', 'mod_2.mod_2', 'mod_2.other_mod_2', 'module.mod_2', 'module.mod_2.mod_2', 'module.mod_2.other_mod_2'])
        
    
    def test_retrieving_classes_from_python_module_in_zip(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))
        module_location = os.path.join(parent_path, 'res_test_robot_session_server', 'module', 'compressed.zip')
        
        classes = _get_classes_from_module(module_location, None, [], [])

        self.assertListEqual(classes, ['compressed.mod_compressed_1', 'compressed.mod_compressed_2'])           
    
    
    def test_retrieving_classes_from_python_module_in_jar(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))
        module_location = os.path.join(parent_path, 'res_test_robot_session_server', 'module', 'compressed.jar')
        
        classes = _get_classes_from_module(module_location, None, [], [])

        self.assertListEqual(classes, ['compressed.mod_compressed_1', 'compressed.mod_compressed_2'])       
    
    
    def test_if_sys_path_is_not_extended_after_retrieving_classes_from_python_module(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))
        module_location = os.path.join(parent_path, 'res_test_robot_session_server', 'module', '__init__.py')
        python_paths = [os.path.join(parent_path, 'res_test_robot_session_server', 'a')]
        class_paths = [os.path.join(parent_path, 'res_test_robot_session_server', 'b')]
        old_sys_path = list(sys.path)
        
        _get_classes_from_module(module_location, None, python_paths, class_paths)

        self.assertCountEqual(old_sys_path, sys.path)       
