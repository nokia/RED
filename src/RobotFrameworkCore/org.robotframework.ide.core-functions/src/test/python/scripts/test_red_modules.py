import unittest
import os

from red_modules import get_modules_search_paths
from red_modules import get_module_path
from red_modules import get_run_module_path


class MudulePathsRetrievingTests(unittest.TestCase):
    def test_if_modules_search_paths_are_returned(self):        
        result = get_modules_search_paths()

        self.assertTrue(len(result) > 0)

    @unittest.skip("enable it after unifying library imports")
    def test_if_module_path_is_returned(self):        
        result = get_module_path('robot.libraries.BuiltIn')

        self.assertTrue(len(result) > 0)

    def test_if_run_module_path_is_returned(self):
        result = get_run_module_path()

        self.assertTrue(len(result) > 0)
