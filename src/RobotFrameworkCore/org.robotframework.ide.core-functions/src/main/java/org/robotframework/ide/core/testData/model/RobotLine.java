package org.robotframework.ide.core.testData.model;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;


public class RobotLine {

    private List<RobotLineType> types = Arrays
            .asList(RobotLineType.UNDECLARED_COMMENT);
    private List<LineElement> elements = new LinkedList<>();


    public List<RobotLineType> getTypes() {
        return types;
    }


    public void setTypes(List<RobotLineType> types) {
        this.types = types;
    }

    public static enum RobotLineType {
        /**
         * unknown line content
         */
        UNDECLARED_COMMENT,
        /**
         * line contains hash sign or comment word
         */
        DECLARED_COMMENT,
        /**
         * <pre>
         * *** Settings ***
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
         * Library import begin declaration
         */
        SETTINGS_IMPORT_LIBRARY,
        /**
         * Resource variables and keywords import begin declaration
         */
        SETTINGS_IMPORT_RESOURCE,
        /**
         * Importing variables file declaration
         */
        SETTINGS_IMPORT_VARIABLES,
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
         * Single keyword line i.e. variable assignment or keyword execution
         */
        KEYWORD_STEP,
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
         * Single test line i.e. variable assignment or keyword execution
         */
        TEST_STEP,
        /**
         * declaration of scalar variable in Variable table
         */
        VARIABLE_SCALAR_DECLARATION,
        /**
         * declaration of list variable in Variable table
         */
        VARIABLE_LIST_DECLARATION,
        /**
         * declaration of dictionary variable in Variable table
         */
        VARIABLE_DICTIONARY_DECLARATION,
        /**
         * its starts from (...)
         */
        CONTINUE_PREVIOUS_LINE;
    }
}
