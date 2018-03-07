import unittest
import os

from red_libraries import get_standard_library_names
from red_libraries import get_standard_library_path
from red_libraries import create_libdoc


class LibrariesRetrievingTests(unittest.TestCase):
    def test_if_standard_library_names_are_returned(self):        
        result = get_standard_library_names()

        self.assertTrue(len(result) > 0)

    def test_if_standard_library_path_is_returned(self):        
        result = get_standard_library_path('BuiltIn')

        self.assertTrue(len(result) > 0)

    def test_if_libdoc_is_returned(self):        
        result = create_libdoc('BuiltIn', 'XML')

        self.assertTrue(len(result) > 0)
