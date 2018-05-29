import unittest

from TestRunnerAgent import _truncate
from TestRunnerAgent import _extract_source_path


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

    def test_extracting_path_to_compiled_python_file(self):
        self.assertEqual('path_to_file.py', _extract_source_path('path_to_file.py'))
        self.assertEqual('path_to_file.py', _extract_source_path('path_to_file.pyc'))
        self.assertEqual('path_to_file.py', _extract_source_path('path_to_file$py.class'))
        self.assertEqual('/path_to_file.py', _extract_source_path('file:/path_to_file.py'))
        self.assertEqual('/path_to_file.py', _extract_source_path('file:/path_to_file.pyc'))
        self.assertEqual('/path_to_file.py', _extract_source_path('file:/path_to_file$py.class'))
