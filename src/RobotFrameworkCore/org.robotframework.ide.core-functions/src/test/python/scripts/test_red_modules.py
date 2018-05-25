import unittest
import os

from red_modules import get_modules_search_paths
from red_modules import get_module_path


class MudulePathsRetrievingTests(unittest.TestCase):
    def test_if_modules_search_paths_are_returned(self):        
        result = get_modules_search_paths()

        self.assertTrue(len(result) > 0)

    def test_if_module_path_is_returned(self):   
        parent_path = os.path.dirname(os.path.realpath(__file__))
        module_path = os.path.join(parent_path, 'res_test_red_modules')

        import importlib
        importlib.import_module('res_test_red_modules')   

        result = get_module_path('res_test_red_modules')

        self.assertEqual(os.path.abspath(result), os.path.abspath(module_path))
