import unittest
import os

from red_libraries import get_standard_library_names
from red_libraries import get_standard_library_path
from red_libraries import create_libdoc
from red_libraries import create_html_doc


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

class HtmlDocTests(unittest.TestCase):
    def test_simple_docs_in_robot_format_are_htmlified(self):
        self.assertEqual(create_html_doc('paragraph', 'ROBOT'), '<p>paragraph</p>')
        self.assertEqual(create_html_doc('line\nline', 'ROBOT'), '<p>line line</p>')
        self.assertEqual(create_html_doc('paragraph\n\nparagraph', 'ROBOT'), '<p>paragraph</p>\n<p>paragraph</p>')
        self.assertEqual(create_html_doc('*bold*', 'ROBOT'), '<p><b>bold</b></p>')
        self.assertEqual(create_html_doc('_italic_', 'ROBOT'), '<p><i>italic</i></p>')
        self.assertEqual(create_html_doc('_*bold italic*_', 'ROBOT'), '<p><i><b>bold italic</b></i></p>')
        self.assertEqual(create_html_doc('``code``', 'ROBOT'), '<p><code>code</code></p>')
        self.assertEqual(create_html_doc('the http://www.rf.org website', 'ROBOT'), 
             '<p>the <a href="http://www.rf.org">http://www.rf.org</a> website</p>')
        self.assertEqual(create_html_doc('= Section =', 'ROBOT'), '<h2>Section</h2>')
        self.assertEqual(create_html_doc('== Section ==', 'ROBOT'), '<h3>Section</h3>')
        self.assertEqual(create_html_doc('= Section =\nparagraph', 'ROBOT'), '<h2>Section</h2>\n<p>paragraph</p>')
        self.assertEqual(create_html_doc('| a | b |\n| 1 |', 'ROBOT'), 
             '<table border="1">\n<tr>\n<td>a</td>\n<td>b</td>\n</tr>\n<tr>\n<td>1</td>\n<td></td>\n</tr>\n</table>')
        self.assertEqual(create_html_doc('- first\n- second', 'ROBOT'), '<ul>\n<li>first</li>\n<li>second</li>\n</ul>')
        self.assertEqual(create_html_doc('| block', 'ROBOT'), '<pre>\nblock\n</pre>')
        self.assertEqual(create_html_doc('line\n---\nline', 'ROBOT'), '<p>line</p>\n<hr>\n<p>line</p>')

    def test_simple_docs_in_text_format_are_htmlified(self):
        self.assertEqual(create_html_doc('paragraph', 'TEXT'), '<p style="white-space: pre-wrap">paragraph</p>')
        self.assertEqual(create_html_doc('line\nline', 'TEXT'), '<p style="white-space: pre-wrap">line\nline</p>')
        self.assertEqual(create_html_doc('paragraph\n\nparagraph', 'TEXT'), 
             '<p style="white-space: pre-wrap">paragraph\n\nparagraph</p>')
        self.assertEqual(create_html_doc('*bold*', 'TEXT'), '<p style="white-space: pre-wrap">*bold*</p>')
        self.assertEqual(create_html_doc('_italic_', 'TEXT'), '<p style="white-space: pre-wrap">_italic_</p>')
        self.assertEqual(create_html_doc('_*bold italic*_', 'TEXT'),
             '<p style="white-space: pre-wrap">_*bold italic*_</p>')
        self.assertEqual(create_html_doc('``code``', 'TEXT'), '<p style="white-space: pre-wrap">``code``</p>')
        self.assertEqual(create_html_doc('the http://www.rf.org website', 'TEXT'), 
             '<p style="white-space: pre-wrap">the <a href="http://www.rf.org">http://www.rf.org</a> website</p>')
        self.assertEqual(create_html_doc('= Section =', 'TEXT'), '<p style="white-space: pre-wrap">= Section =</p>')
        self.assertEqual(create_html_doc('== Section ==', 'TEXT'), '<p style="white-space: pre-wrap">== Section ==</p>')
        self.assertEqual(create_html_doc('= Section =\nparagraph', 'TEXT'), 
             '<p style="white-space: pre-wrap">= Section =\nparagraph</p>')
        self.assertEqual(create_html_doc('| a | b |\n| 1 |', 'TEXT'), 
             '<p style="white-space: pre-wrap">| a | b |\n| 1 |</p>')
        self.assertEqual(create_html_doc('- first\n- second', 'TEXT'), 
             '<p style="white-space: pre-wrap">- first\n- second</p>')
        self.assertEqual(create_html_doc('| block', 'TEXT'), '<p style="white-space: pre-wrap">| block</p>')
        self.assertEqual(create_html_doc('line\n---\nline', 'TEXT'),
             '<p style="white-space: pre-wrap">line\n---\nline</p>')

    def test_simple_docs_in_html_format_are_htmlified(self):
        self.assertEqual(create_html_doc('paragraph', 'HTML'), '<div style="margin: 0">paragraph</div>')
        self.assertEqual(create_html_doc('line\nline', 'HTML'), '<div style="margin: 0">line\nline</div>')
        self.assertEqual(create_html_doc('paragraph\n\nparagraph', 'HTML'), 
             '<div style="margin: 0">paragraph\n\nparagraph</div>')
        self.assertEqual(create_html_doc('*bold*', 'HTML'), '<div style="margin: 0">*bold*</div>')
        self.assertEqual(create_html_doc('<b>bold</b>', 'HTML'), '<div style="margin: 0"><b>bold</b></div>')
        self.assertEqual(create_html_doc('_italic_', 'HTML'), '<div style="margin: 0">_italic_</div>')
        self.assertEqual(create_html_doc('<i>italic</i>', 'HTML'), '<div style="margin: 0"><i>italic</i></div>')
        self.assertEqual(create_html_doc('_*bold italic*_', 'HTML'), '<div style="margin: 0">_*bold italic*_</div>')
        self.assertEqual(create_html_doc('<i><b>bold italic</b></i>', 'HTML'), 
             '<div style="margin: 0"><i><b>bold italic</b></i></div>')
        self.assertEqual(create_html_doc('``code``', 'HTML'), '<div style="margin: 0">``code``</div>')
        self.assertEqual(create_html_doc('<code>code</code>', 'HTML'), '<div style="margin: 0"><code>code</code></div>')
        self.assertEqual(create_html_doc('the http://www.rf.org website', 'HTML'), 
             '<div style="margin: 0">the http://www.rf.org website</div>')
        self.assertEqual(create_html_doc('the <a href="http://www.rf.org">http://www.rf.org</a> website', 'HTML'), 
             '<div style="margin: 0">the <a href="http://www.rf.org">http://www.rf.org</a> website</div>')
        self.assertEqual(create_html_doc('= Section =', 'HTML'), '<div style="margin: 0">= Section =</div>')
        self.assertEqual(create_html_doc('<h2>Section</h2>', 'HTML'), '<div style="margin: 0"><h2>Section</h2></div>')
        self.assertEqual(create_html_doc('== Section ==', 'HTML'), '<div style="margin: 0">== Section ==</div>')
        self.assertEqual(create_html_doc('<h3>Section</h3>', 'HTML'), '<div style="margin: 0"><h3>Section</h3></div>')
        