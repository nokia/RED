import unittest
import xmlrunner
from util import add_one

class AdderTests(unittest.TestCase):

    def test_add(self):
        self.assertEqual(5, add_one(4))