import unittest
import xmlrunner

from TestRunnerAgent import _truncate

class AdderTests(unittest.TestCase):
    
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