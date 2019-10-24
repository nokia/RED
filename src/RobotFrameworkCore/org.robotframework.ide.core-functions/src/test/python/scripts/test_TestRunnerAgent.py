import unittest

from TestRunnerAgent import _truncate
from TestRunnerAgent import _extract_source_path
from TestRunnerAgent import _collect_children_paths



class TruncationTests(unittest.TestCase):

    def test_message_is_untouched_if_holds_limit(self):
        limit = 99
        message = 'msg' * 20

        self.assertEqual('msg' * 20, _truncate(limit, message))

    def test_message_is_untouched_if_equals_limit(self):
        limit = 99
        message = 'msg' * 33

        self.assertEqual('msg' * 33, _truncate(limit, message))

    def test_message_is_truncated_if_exceeds_limit(self):
        limit = 99
        message = 'msg' * limit

        self.assertEqual(('msg' * 33) + ' <truncated>', _truncate(limit, message))


class SourcePathExtractionTests(unittest.TestCase):

    def test_extracting_path_to_jar(self):
        self.assertEqual('path_to_file.jar', _extract_source_path('path_to_file.jar'))
        self.assertEqual('/path_to_file.jar', _extract_source_path('file:/path_to_file.jar'))
        self.assertEqual('/path_to_file.jar', _extract_source_path('jar:file:/path_to_file.jar'))
        self.assertEqual('/path_to_file.jar', _extract_source_path('jar:file:/path_to_file.jar!/JavaClass.class'))
        self.assertEqual('/path_to_file.jar', _extract_source_path('file:/path_to_file.jar/PyModule.py'))
        self.assertEqual('/path_to_file.jar', _extract_source_path('file:/path_to_file.jar\\PyModule.py'))
        
        self.assertEqual('path_to_file.JAR', _extract_source_path('path_to_file.JAR'))
        self.assertEqual('/path_to_file.JAR', _extract_source_path('file:/path_to_file.JAR'))
        self.assertEqual('/path_to_file.JAR', _extract_source_path('jar:file:/path_to_file.JAR'))
        self.assertEqual('/path_to_file.JAR', _extract_source_path('jar:file:/path_to_file.JAR!/JavaClass.class'))
        self.assertEqual('/path_to_file.JAR', _extract_source_path('file:/path_to_file.JAR/PyModule.py'))
        self.assertEqual('/path_to_file.JAR', _extract_source_path('file:/path_to_file.JAR\\PyModule.py'))

    def test_extracting_path_to_compiled_python_file(self):
        self.assertEqual('path_to_file.py', _extract_source_path('path_to_file.py'))
        self.assertEqual('path_to_file.py', _extract_source_path('path_to_file.pyc'))
        self.assertEqual('path_to_file.py', _extract_source_path('path_to_file$py.class'))
        self.assertEqual('/path_to_file.py', _extract_source_path('file:/path_to_file.py'))
        self.assertEqual('/path_to_file.py', _extract_source_path('file:/path_to_file.pyc'))
        self.assertEqual('/path_to_file.py', _extract_source_path('file:/path_to_file$py.class'))


class ChildrenPathsCollectingTests(unittest.TestCase):

    def test_collect_children_paths_from_root_node(self):
        import os
        
        parent_path = os.path.dirname(os.path.realpath(__file__))
        
        suites = ['First Child', 'Second Child']
        source = os.path.join(parent_path, 'res_test_TestRunnerAgent', 'suite')
        
        self.assertEqual([os.path.join(source, 'first_child'), os.path.join(source, 'second_child')], _collect_children_paths(suites, source))
        
    def test_collect_children_paths_of_same_name_suites(self):
        import os
        
        parent_path = os.path.dirname(os.path.realpath(__file__))
        
        suites = ['Suite', 'Suite']
        source = os.path.join(parent_path, 'res_test_TestRunnerAgent', 'same_names')
        
        collected =  _collect_children_paths(suites, source)
        self.assertEqual([os.path.join(source, '000__suite'), os.path.join(source, '001__suite')], collected)
        
    def test_none_is_returned_for_non_existing_suites(self):
        import os
        
        parent_path = os.path.dirname(os.path.realpath(__file__))
        
        suites = ['Non Existing']
        source = os.path.join(parent_path, 'res_test_TestRunnerAgent')
        
        collected =  _collect_children_paths(suites, source)
        self.assertEqual([None], collected)
    

    def test_collect_children_paths_from_child_node(self):
        import os
        
        parent_path = os.path.dirname(os.path.realpath(__file__))
        
        suites = ['Suite File']
        source = os.path.join(parent_path, 'res_test_TestRunnerAgent', 'suite', 'first_child')
        
        self.assertEqual([os.path.join(source, 'suite_file.robot')], _collect_children_paths(suites, source))

    def test_collect_children_paths_from_suite_file(self):
        import os
        
        parent_path = os.path.dirname(os.path.realpath(__file__))
        
        suites = []
        source = os.path.join(parent_path, 'res_test_TestRunnerAgent', 'suite', 'first_child', 'suite_file.robot')
        
        self.assertEqual([], _collect_children_paths(suites, source))
        
