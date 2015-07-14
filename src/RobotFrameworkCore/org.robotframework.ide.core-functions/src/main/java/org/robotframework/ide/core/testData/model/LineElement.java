package org.robotframework.ide.core.testData.model;

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
         * just {@code :for}
         */
        FOR_LOOP_DECLARATION,
        /**
         * it comes just after {@code :for} its variable to use inside loop
         */
        FOR_INDEX,
        /**
         * just {@code IN} mandatory and after optional {@code RANGE} and etc.
         */
        FOR_LOOP_ITERATOR_DECLARATION,
        /**
         * {@code \ action arguments..}
         */
        FOR_LOOP_CONTINUE,

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
         * Library import begin declaration
         */
        IMPORT_LIBRARY,
        /**
         * name of imported library or path to it, for resource and variables is
         * path
         */
        IMPORT_LOCATION,
        /**
         * argument pass to library during initialization
         */
        ARGUMENT,
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
         * Variables file import begin declaration
         */
        IMPORT_VARIABLES,

        /**
         * Documentation for suite declaration
         */
        SETTINGS_DOCUMENTATION,
        /**
         * User key {separator} value additional information about suite
         */
        SETTINGS_METADATA,
        /**
         * 
         */
        METADATA_KEY,
        /**
         * 
         */
        METADATA_VALUE,
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
        TEST_CASE_NAME,

        /**
         * in context param1=${value} its represent param1
         */
        ARGUMENT_PARAMETER_NAME,
        /**
         * equals
         */
        ARGUMENT_EQUALS,
        /**
         * in context param1=${value} its represent ${value} - in case
         * {@link #ARGUMENT_PARAMETER_NAME} is not present it represents also
         * value
         */
        ARGUMENT_PARAMETER_VALUE;
    }
}
