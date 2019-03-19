import unittest
import red_pydevd_package.redpydevd
import red_pydevd_package.redpydevd.redpydevd

class VariablesRetrievingTests(unittest.TestCase):
    
    def test_version_of_parent_module_should_be_the_same_as_actual_redpydevd_module(self):
        parent_module_version = red_pydevd_package.redpydevd.__version_info__ 
        module_version = red_pydevd_package.redpydevd.redpydevd.__version_info__
        
        self.assertEqual(parent_module_version, module_version)