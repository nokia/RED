import unittest
import os

from red_library_autodiscover import _collect_source_paths


class LibraryAutodiscoveringTests(unittest.TestCase):
    def test_if_empty_paths_are_returned_for_project_without_libs(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))
        project_location = os.path.join(parent_path, 'res_test_red_library_autodiscover', 'no_libs')

        python_paths, class_paths = _collect_source_paths(project_location)

        self.assertEqual(python_paths, [])
        self.assertEqual(class_paths, [])

    def test_if_all_paths_with_python_files_are_collected(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))
        project_location = os.path.join(parent_path, 'res_test_red_library_autodiscover', 'python_libs')

        python_paths, class_paths = _collect_source_paths(project_location)

        self.assertEqual(python_paths, [project_location,
                                        os.path.join(project_location, 'a'),
                                        os.path.join(project_location, 'a', 'b'),
                                        os.path.join(project_location, 'a', 'b', 'c'),
                                        os.path.join(project_location, 'other')])
        self.assertEqual(class_paths, [])

    def test_if_only_paths_with_python_files_are_collected(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))
        project_location = os.path.join(parent_path, 'res_test_red_library_autodiscover', 'python_libs_and_robot_files')

        python_paths, class_paths = _collect_source_paths(project_location)

        self.assertEqual(python_paths, [os.path.join(project_location, 'libs')])
        self.assertEqual(class_paths, [])

    def test_if_paths_with_multiple_libs_are_collected_only_once(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))
        project_location = os.path.join(parent_path, 'res_test_red_library_autodiscover', 'python_libs_multiple')

        python_paths, class_paths = _collect_source_paths(project_location)

        self.assertEqual(python_paths, [project_location, os.path.join(project_location, 'libs')])
        self.assertEqual(class_paths, [])

    def test_if_python_and_java_libs_are_collected(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))
        project_location = os.path.join(parent_path, 'res_test_red_library_autodiscover', 'python_libs_and_java_libs')

        python_paths, class_paths = _collect_source_paths(project_location)

        self.assertEqual(python_paths, [os.path.join(project_location, 'libs')])
        import platform
        if 'Jython' in platform.python_implementation():
            self.assertEqual(class_paths, [os.path.join(project_location, 'libs', 'LibA.jar'),
                                           os.path.join(project_location, 'other', 'LibB.jar')])
        else:
            self.assertEqual(class_paths, [])

    def test_if_nested_paths_are_not_collected_when_search_is_not_recursive(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))
        project_location = os.path.join(parent_path, 'res_test_red_library_autodiscover', 'python_libs')

        python_paths, class_paths = _collect_source_paths(project_location, False)

        self.assertEqual(python_paths, [project_location,
                                        os.path.join(project_location, 'a'),
                                        os.path.join(project_location, 'other')])
        self.assertEqual(class_paths, [])

    def test_if_excluded_paths_are_not_collected(self):
        parent_path = os.path.dirname(os.path.realpath(__file__))
        project_location = os.path.join(parent_path, 'res_test_red_library_autodiscover', 'python_libs')

        python_paths, class_paths = _collect_source_paths(project_location, True, ['lib.py', 'other'])

        self.assertEqual(python_paths, [os.path.join(project_location, 'a'),
                                        os.path.join(project_location, 'a', 'b'),
                                        os.path.join(project_location, 'a', 'b', 'c')])
        self.assertEqual(class_paths, [])
