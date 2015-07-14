package org.robotframework.ide.core.testData.model.table;

import java.util.Arrays;
import java.util.List;


public class LineElement {

    private List<ElementType> type = Arrays.asList(ElementType.VALUE);
    private StringBuilder value = new StringBuilder();

    public enum ElementType {
        /**
         * just text without any special meaning
         */
        VALUE,
        /**
         * Comment word or hash sign not escaped
         */
        DECLARED_COMMENT,
        /**
         * <pre>
         * *** Settings *** or *** Metadata ***
         * </pre>
         * 
         * setting table declaration
         */
        SETTING_TABLE_HEADER,
        /**
         * <pre>
         * *** Variables ***
         * </pre>
         * 
         * variable table declaration
         */
        VARIABLE_TABLE_HEADER,
        /**
         * <pre>
         * *** Test Case ***
         * </pre>
         * 
         * variable table declaration
         */
        TEST_CASE_TABLE_HEADER,
        /**
         * <pre>
         * *** Keywords ***
         * </pre>
         * 
         * keyword table declaration
         */
        KEYWORD_TABLE_HEADER,
        /**
         * own defined by user column name comes after table declaration i.e.
         * 
         * <pre>
         * *** Test Case *** *** Action ***
         * </pre>
         */
        TABLE_COLUMN_NAME,

        /**
         * its is usually defined before keyword and ends with dot single sign
         */
        LIBRARY_CONTAINS_KEYWORD,
        /**
         * declaration of keyword name
         */
        KEYWORD_NAME,
        /**
         * argument to pass to keyword
         */
        KEYWORD_ARGUMENT,

        /**
         * scalar variable in use
         */
        VARIABLE_SCALAR,
        /**
         * list variable in use
         */
        VARIABLE_LIST,
        /**
         * dictionary variable in use
         */
        VARIABLE_DICTIONARY,

        /**
         * appears after time setting
         */
        TIMEOUT_VALUE,
        /**
         * value used for tagging
         */
        TAG_VALUE,

        /**
         * Library import begin declaration
         */
        IMPORT_LIBRARY,
        /**
         * name of imported library or path to it
         */
        IMPORT_LIBRARY_NAME_OR_PATH,
        /**
         * argument pass to library during initialization
         */
        IMPORT_LIBRARY_INITAL_ARGUMENT,
        /**
         * 'with name' alias words
         */
        IMPORT_LIBRARY_ALIASES,
        /**
         * alias name of imported library comes after 'with name'
         */
        IMPORT_LIBRARY_ALIAS_NAME,

        /**
         * Resource import begin declaration
         */
        IMPORT_RESOURCE,
        /**
         * path to imported resource
         */
        IMPORT_RESOURCE_PATH,

        /**
         * Variables file import begin declaration
         */
        IMPORT_VARIABLES,
        /**
         * path to imported variables
         */
        IMPORT_VARIABLES_PATH,
        /**
         * argument pass to variable during initialization - mostly for python
         * script
         */
        IMPORT_VARIABLES_INITAL_ARGUMENT,

        /**
         * Documentation for suite declaration
         */
        SETTINGS_DOCUMENTATION,
        /**
         * User key {separator} value additional information about suite
         */
        SETTINGS_METADATA,
        /**
         * Suite Setup keyword
         */
        SETTINGS_SUITE_SETUP,
        /**
         * Suite Teardown keyword
         */
        SETTINGS_SUITE_TEARDOWN,
        /**
         * the same as {@link #SETTINGS_SUITE_SETUP}
         */
        SETTINGS_SUITE_PRECONDITION,
        /**
         * the same as {@link #SETTINGS_SUITE_TEARDOWN}
         */
        SETTINGS_SUITE_POSTCONDITION,
        /**
         * Used for specifying forced values for tags when tagging test cases.
         */
        SETTINGS_FORCE_TAGS,
        /**
         * Used for specifying default values for tags when tagging test cases.
         */
        SETTINGS_DEFAULT_TAGS,

        /**
         * default setup for all test cases in suite
         */
        SETTINGS_TEST_SETUP,
        /**
         * default teardown for all test cases in suite
         */
        SETTINGS_TEST_TEARDOWN,
        /**
         * the same as {@link #TABLE_SETTINGS_TEST_SETUP}
         */
        SETTINGS_TEST_PRECONDITION,
        /**
         * the same as {@link #TABLE_SETTINGS_SUITE_TEARDOWN}
         */
        SETTINGS_TEST_POSTCONDITION,
        /**
         * default template keyword for test cases in data-driven tests
         */
        SETTINGS_TEST_TEMPLATE,
        /**
         * default test case timeout
         */
        SETTINGS_TEST_TIMEOUT,

        /**
         * Used for specifying a user keyword documentation, appears as
         * {@code [Documentation]}
         */
        KEYWORD_DOCUMENTATION,
        /**
         * Used for specifying user keyword arguments, appears as
         * {@code [Arguments]}
         */
        KEYWORD_ARGUMENTS,
        /**
         * Used for specifying user keyword return values, appears as
         * {@code [Return]}
         */
        KEYWORD_RETURN,
        /**
         * Used for specifying user keyword teardown, appears as
         * {@code [Teardown]}
         */
        KEYWORD_TEARDOWN,
        /**
         * Used for specifying a user keyword timeout, appears as
         * {@code [Timeout]}
         */
        KEYWORD_TIMEOUT,
        /**
         * name defined by user
         */
        USER_KEYWORD_NAME,

        /**
         * Used for specifying a test case documentation, appears as
         * {@code [Documentation]}
         */
        TEST_CASE_DOCUMENTATION,
        /**
         * Used for tagging test cases, appears as {@code [Tags]}
         */
        TEST_CASE_TAGS,
        /**
         * Used for specifying a test setup, appears as {@code [Setup]}
         */
        TEST_CASE_SETUP,
        /**
         * Used for specifying a test teardown, appears as {@code [Teardown]}
         */
        TEST_CASE_TEARDOWN,
        /**
         * A synonym for {@code [Setup]} {@link #TEST_CASE_SETUP}
         */
        TEST_CASE_PRECONDITION,
        /**
         * A synonym for {@code [Teardown]} {@link #TEST_CASE_TEARDOWN}
         */
        TEST_CASE_POSTCONDITION,
        /**
         * Used for specifying a template keyword, appears as {@code [Template]}
         */
        TEST_CASE_TEMPLATE,
        /**
         * Used for specifying a test case timeout, appears as {@code [Timeout]}
         */
        TEST_CASE_TIMEOUT,
        /**
         * name defined by user
         */
        TEST_CASE_NAME;
    }
}
