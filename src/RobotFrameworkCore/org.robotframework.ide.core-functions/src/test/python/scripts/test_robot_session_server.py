import unittest
import sys
import os

from robot_session_server import _create_libdoc

class LibdocGenerationTests(unittest.TestCase):
    
    def test_subsequent_lidocs_for_same_name_libs_under_different_paths_returns_different_libdocs(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))
        
        libdoc1 = _create_libdoc("lib", [os.path.join(parent_path, 'res_test_robot_session_server', 'a')], [])
        libdoc2 = _create_libdoc("lib", [os.path.join(parent_path, 'res_test_robot_session_server', 'b')], [])
        
        self.assertNotEqual(libdoc1, libdoc2)
