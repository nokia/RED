/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.text.read.recognizer;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.text.read.IRobotTokenType;

import com.google.common.collect.LinkedListMultimap;


public enum RobotTokenType implements IRobotTokenType {
    /**
     * 
     */
    UNKNOWN(TableType.NOT_STRICTLY_BELONGS),
    /**
     * 
     */
    EMPTY_CELL(TableType.NOT_STRICTLY_BELONGS, " \\ "),
    /**
     * 
     */
    VARIABLE_USAGE(TableType.NOT_STRICTLY_BELONGS),
    /**
     * 
     */
    PRETTY_ALIGN_SPACE(TableType.NOT_STRICTLY_BELONGS, " "),
    /**
     * 
     */
    USER_OWN_TABLE_HEADER(TableType.NOT_STRICTLY_BELONGS),
    /**
     * 
     */
    SETTINGS_TABLE_HEADER(TableType.SETTINGS, "Setting", "Settings", "Metadata"),
    /**
     * 
     */
    VARIABLES_TABLE_HEADER(TableType.VARIABLES, "Variable", "Variables"),
    /**
     * 
     */
    TEST_CASES_TABLE_HEADER(TableType.TEST_CASES, "Test Case", "Test Cases"),
    /**
    * 
    */
    KEYWORDS_TABLE_HEADER(TableType.KEYWORDS, "Keyword", "Keywords",
            "User Keyword", "User Keywords"),
    /**
     * 
     */
    TABLE_HEADER_COLUMN(TableType.NOT_STRICTLY_BELONGS),
    /**
     * 
     */
    START_HASH_COMMENT(TableType.NOT_STRICTLY_BELONGS, "#"),
    /**
     * 
     */
    COMMENT_CONTINUE(TableType.NOT_STRICTLY_BELONGS),
    /**
     * 
     */
    PREVIOUS_LINE_CONTINUE(TableType.NOT_STRICTLY_BELONGS, "..."),
    /**
     * 
     */
    SETTING_LIBRARY_DECLARATION(TableType.SETTINGS, "Library", "Library:"),
    /**
     * 
     */
    SETTING_UNKNOWN(TableType.SETTINGS),
    /**
     * 
     */
    SETTING_UNKNOWN_ARGUMENT(TableType.SETTINGS),
    /**
     * 
     */
    SETTING_LIBRARY_NAME(TableType.SETTINGS),
    /**
     * 
     */
    SETTING_LIBRARY_ARGUMENT(TableType.SETTINGS),
    /**
     * 
     */
    SETTING_LIBRARY_ALIAS(TableType.SETTINGS, "WITH NAME"),
    /**
     * 
     */
    SETTING_LIBRARY_ALIAS_VALUE(TableType.SETTINGS),
    /**
     * 
     */
    SETTING_VARIABLES_DECLARATION(TableType.SETTINGS, "Variables", "Variables:"),
    /**
     * 
     */
    SETTING_VARIABLES_FILE_NAME(TableType.SETTINGS),
    /**
     * 
     */
    SETTING_VARIABLES_ARGUMENT(TableType.SETTINGS),
    /**
     * 
     */
    SETTING_RESOURCE_DECLARATION(TableType.SETTINGS, "Resource", "Resource:"),
    /**
     * 
     */
    SETTING_RESOURCE_FILE_NAME(TableType.SETTINGS),
    /**
     * 
     */
    SETTING_RESOURCE_UNWANTED_ARGUMENT(TableType.SETTINGS),
    /**
     * 
     */
    SETTING_DOCUMENTATION_DECLARATION(TableType.SETTINGS, "Documentation",
            "Documentation:"),
    /**
     * 
     */
    SETTING_DOCUMENTATION_TEXT(TableType.SETTINGS),
    /**
     * 
     */
    SETTING_METADATA_DECLARATION(TableType.SETTINGS, "Metadata", "Metadata:"),
    /**
     * 
     */
    SETTING_METADATA_KEY(TableType.SETTINGS),
    /**
     * 
     */
    SETTING_METADATA_VALUE(TableType.SETTINGS),
    /**
     * 
     */
    SETTING_SUITE_SETUP_DECLARATION(TableType.SETTINGS, "Suite Setup",
            "Suite Setup:", "Suite Precondition", "Suite Precondition:"),
    /**
     * 
     */
    SETTING_SUITE_SETUP_KEYWORD_NAME(TableType.SETTINGS),
    /**
     * 
     */
    SETTING_SUITE_SETUP_KEYWORD_ARGUMENT(TableType.SETTINGS),
    /**
     * 
     */
    SETTING_SUITE_TEARDOWN_DECLARATION(TableType.SETTINGS, "Suite Teardown",
            "Suite Teardown:", "Suite Postcondition", "Suite Postcondition:"),
    /**
     * 
     */
    SETTING_SUITE_TEARDOWN_KEYWORD_NAME(TableType.SETTINGS),
    /**
     * 
     */
    SETTING_SUITE_TEARDOWN_KEYWORD_ARGUMENT(TableType.SETTINGS),
    /**
     * 
     */
    SETTING_FORCE_TAGS_DECLARATION(TableType.SETTINGS, "Force Tags",
            "Force Tags:"),
    /**
     * 
     */
    SETTING_FORCE_TAG(TableType.SETTINGS),
    /**
     * 
     */
    SETTING_DEFAULT_TAGS_DECLARATION(TableType.SETTINGS, "Default Tags",
            "Default Tags:"),
    /**
     * 
     */
    SETTING_DEFAULT_TAG(TableType.SETTINGS),
    /**
     * 
     */
    SETTING_TEST_SETUP_DECLARATION(TableType.SETTINGS, "Test Setup",
            "Test Setup:", "Test Precondition", "Test Precondition:"),
    /**
     * 
     */
    SETTING_TEST_SETUP_KEYWORD_NAME(TableType.SETTINGS),
    /**
     * 
     */
    SETTING_TEST_SETUP_KEYWORD_ARGUMENT(TableType.SETTINGS),
    /**
     * 
     */
    SETTING_TEST_TEARDOWN_DECLARATION(TableType.SETTINGS, "Test Teardown",
            "Test Teardown:", "Test Postcondition", "Test Postcondition:"),
    /**
     * 
     */
    SETTING_TEST_TEARDOWN_KEYWORD_NAME(TableType.SETTINGS),
    /**
     * 
     */
    SETTING_TEST_TEARDOWN_KEYWORD_ARGUMENT(TableType.SETTINGS),
    /**
     * 
     */
    SETTING_TEST_TEMPLATE_DECLARATION(TableType.SETTINGS, "Test Template",
            "Test Template:"),
    /**
     * 
     */
    SETTING_TEST_TEMPLATE_KEYWORD_NAME(TableType.SETTINGS),
    /**
     * 
     */
    SETTING_TEST_TEMPLATE_KEYWORD_UNWANTED_ARGUMENT(TableType.SETTINGS),
    /**
     * 
     */
    SETTING_TEST_TIMEOUT_DECLARATION(TableType.SETTINGS, "Test Timeout",
            "Test Timeout:"),
    /**
     * 
     */
    SETTING_TEST_TIMEOUT_VALUE(TableType.SETTINGS),
    /**
     * 
     */
    SETTING_TEST_TIMEOUT_MESSAGE(TableType.SETTINGS),
    /**
     * 
     */
    VARIABLES_SCALAR_DECLARATION(TableType.VARIABLES),
    /**
     * 
     */
    VARIABLES_SCALAR_AS_LIST_DECLARATION(TableType.VARIABLES),
    /**
     * 
     */
    VARIABLES_LIST_DECLARATION(TableType.VARIABLES),
    /**
     * 
     */
    VARIABLES_DICTIONARY_DECLARATION(TableType.VARIABLES),
    /**
     * 
     */
    VARIABLES_UNKNOWN_DECLARATION(TableType.VARIABLES),
    /**
     * 
     */
    VARIABLES_VARIABLE_VALUE(TableType.VARIABLES),
    /**
     * 
     */
    VARIABLES_DICTIONARY_KEY(TableType.VARIABLES),
    /**
     * 
     */
    VARIABLES_DICTIONARY_VALUE(TableType.VARIABLES),
    /**
     * 
     */
    TEST_CASE_SETTING_DOCUMENTATION(TableType.TEST_CASES, "[Documentation]"),
    /**
     * 
     */
    TEST_CASE_SETTING_DOCUMENTATION_TEXT(TableType.TEST_CASES),
    /**
     * 
     */
    TEST_CASE_SETTING_TAGS_DECLARATION(TableType.TEST_CASES, "[Tags]"),
    /**
     * 
     */
    TEST_CASE_SETTING_TAGS(TableType.TEST_CASES),
    /**
     * 
     */
    TEST_CASE_SETTING_SETUP(TableType.TEST_CASES, "[Setup]", "[Precondition]"),
    /**
     * 
     */
    TEST_CASE_SETTING_SETUP_KEYWORD_NAME(TableType.TEST_CASES),
    /**
     * 
     */
    TEST_CASE_SETTING_SETUP_KEYWORD_ARGUMENT(TableType.TEST_CASES),
    /**
     * 
     */
    TEST_CASE_SETTING_TEARDOWN(TableType.TEST_CASES, "[Teardown]",
            "[Postcondition]"),
    /**
     * 
     */
    TEST_CASE_SETTING_TEARDOWN_KEYWORD_NAME(TableType.TEST_CASES),
    /**
     * 
     */
    TEST_CASE_SETTING_TEARDOWN_KEYWORD_ARGUMENT(TableType.TEST_CASES),
    /**
     * 
     */
    TEST_CASE_SETTING_TEMPLATE(TableType.TEST_CASES, "[Template]"),
    /**
     * 
     */
    TEST_CASE_SETTING_TEMPLATE_KEYWORD_NAME(TableType.TEST_CASES),
    /**
     * 
     */
    TEST_CASE_SETTING_TEMPLATE_KEYWORD_UNWANTED_ARGUMENT(TableType.TEST_CASES),
    /**
     * 
     */
    TEST_CASE_SETTING_TIMEOUT(TableType.TEST_CASES, "[Timeout]"),
    /**
     * 
     */
    TEST_CASE_SETTING_TIMEOUT_VALUE(TableType.TEST_CASES),
    /**
     * 
     */
    TEST_CASE_SETTING_TIMEOUT_MESSAGE(TableType.TEST_CASES),
    /**
     * 
     */
    TEST_CASE_NAME(TableType.TEST_CASES),
    /**
     * 
     */
    TEST_CASE_ACTION_NAME(TableType.TEST_CASES),
    /**
     * 
     */
    TEST_CASE_ACTION_ARGUMENT(TableType.TEST_CASES),
    /**
     * 
     */
    KEYWORD_SETTING_DOCUMENTATION(TableType.KEYWORDS, "[Documentation]"),
    /**
     * 
     */
    KEYWORD_SETTING_DOCUMENTATION_TEXT(TableType.KEYWORDS),
    /**
     * 
     */
    KEYWORD_SETTING_TAGS(TableType.KEYWORDS, "[Tags]"),
    /**
     * 
     */
    KEYWORD_SETTING_TAGS_TAG_NAME(TableType.KEYWORDS),
    /**
     * 
     */
    KEYWORD_SETTING_ARGUMENTS(TableType.KEYWORDS, "[Arguments]"),
    /**
     * 
     */
    KEYWORD_SETTING_ARGUMENT(TableType.KEYWORDS),
    /**
     * 
     */
    KEYWORD_SETTING_RETURN(TableType.KEYWORDS, "[Return]"),
    /**
     * 
     */
    KEYWORD_SETTING_RETURN_VALUE(TableType.KEYWORDS),
    /**
     * 
     */
    KEYWORD_SETTING_TEARDOWN(TableType.KEYWORDS, "[Teardown]"),
    /**
     * 
     */
    KEYWORD_SETTING_TEARDOWN_KEYWORD_NAME(TableType.KEYWORDS),
    /**
     * 
     */
    KEYWORD_SETTING_TEARDOWN_KEYWORD_ARGUMENT(TableType.KEYWORDS),

    /**
     * 
     */
    KEYWORD_SETTING_TIMEOUT(TableType.KEYWORDS, "[Timeout]"),
    /**
     * 
     */
    KEYWORD_SETTING_TIMEOUT_VALUE(TableType.KEYWORDS),
    /**
     * 
     */
    KEYWORD_SETTING_TIMEOUT_MESSAGE(TableType.KEYWORDS),
    /**
     * 
     */
    KEYWORD_NAME(TableType.KEYWORDS),
    /**
     * 
     */
    KEYWORD_ACTION_NAME(TableType.KEYWORDS),
    /**
     * 
     */
    KEYWORD_ACTION_ARGUMENT(TableType.KEYWORDS);

    private final List<String> representationForNew = new LinkedList<>();
    private final TableType type;
    private static final LinkedListMultimap<TableType, RobotTokenType> TYPE_TO_TABLE = LinkedListMultimap
            .create();
    static {
        RobotTokenType[] values = RobotTokenType.values();
        for (RobotTokenType type : values) {
            TYPE_TO_TABLE.put(type.type, type);
        }
    }


    public List<String> getRepresentation() {
        return representationForNew;
    }


    private RobotTokenType(TableType type, String... representation) {
        this.type = type;
        representationForNew.addAll(Arrays.asList(representation));
    }

    private enum TableType {
        NOT_STRICTLY_BELONGS, SETTINGS, VARIABLES, TEST_CASES, KEYWORDS;
    }


    public List<RobotTokenType> getTypesForSettingsTable() {
        return getTypes(TableType.SETTINGS);
    }


    public List<RobotTokenType> getTypesForVariablesTable() {
        return getTypes(TableType.VARIABLES);
    }


    public List<RobotTokenType> getTypesForTestCasesTable() {
        return getTypes(TableType.TEST_CASES);
    }


    public List<RobotTokenType> getTypesForKeywordsTable() {
        return getTypes(TableType.KEYWORDS);
    }


    public List<RobotTokenType> getTypesNotStrictlyBelongs() {
        return getTypes(TableType.NOT_STRICTLY_BELONGS);
    }


    private List<RobotTokenType> getTypes(final TableType type) {
        List<RobotTokenType> p = new LinkedList<>(TYPE_TO_TABLE.get(type));

        return p;
    }
}