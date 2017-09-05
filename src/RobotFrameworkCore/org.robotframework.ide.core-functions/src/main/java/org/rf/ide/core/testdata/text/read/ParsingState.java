/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.read;

import java.util.ArrayList;
import java.util.List;


public enum ParsingState {
    /**
     * 
     */
    UNKNOWN(null, null),
    /**
     * 
     */
    TRASH(null, null),
    /**
     * 
     */
    TABLE_HEADER_COLUMN(null, null),
    /**
     * 
     */
    COMMENT(null, null),
    /**
     * 
     */
    SETTING_TABLE_HEADER(TableType.SETTINGS, null),
    /**
     * 
     */
    SETTING_TABLE_INSIDE(TableType.SETTINGS, SETTING_TABLE_HEADER),
    /**
     * 
     */
    VARIABLE_TABLE_HEADER(TableType.VARIABLES, null),
    /**
     * 
     */
    VARIABLE_TABLE_INSIDE(TableType.VARIABLES, VARIABLE_TABLE_HEADER),
    /**
     * 
     */
    TEST_CASE_TABLE_HEADER(TableType.TEST_CASE, null),
    /**
     * 
     */
    TEST_CASE_TABLE_INSIDE(TableType.TEST_CASE, TEST_CASE_TABLE_HEADER),
    /**
     * 
     */
    KEYWORD_TABLE_HEADER(TableType.KEYWORD, null),
    /**
     * 
     */
    KEYWORD_TABLE_INSIDE(TableType.KEYWORD, KEYWORD_TABLE_HEADER),
    /**
     * 
     */
    SETTING_LIBRARY_IMPORT(TableType.SETTINGS, SETTING_TABLE_INSIDE),
    /**
     * 
     */
    SETTING_LIBRARY_NAME_OR_PATH(TableType.SETTINGS, SETTING_LIBRARY_IMPORT),
    /**
     * 
     */
    SETTING_LIBRARY_ARGUMENTS(TableType.SETTINGS, SETTING_LIBRARY_NAME_OR_PATH),
    /**
     * 
     */
    SETTING_LIBRARY_IMPORT_ALIAS(TableType.SETTINGS, SETTING_LIBRARY_ARGUMENTS),
    /**
     * 
     */
    SETTING_LIBRARY_IMPORT_ALIAS_VALUE(TableType.SETTINGS,
            SETTING_LIBRARY_IMPORT_ALIAS),
    /**
     * 
     */
    SETTING_VARIABLE_IMPORT(TableType.SETTINGS, SETTING_TABLE_INSIDE),
    /**
     * 
     */
    SETTING_VARIABLE_IMPORT_PATH(TableType.SETTINGS, SETTING_VARIABLE_IMPORT),
    /**
     * 
     */
    SETTING_VARIABLE_ARGUMENTS(TableType.SETTINGS, SETTING_VARIABLE_IMPORT_PATH),
    /**
     * 
     */
    SETTING_RESOURCE_IMPORT(TableType.SETTINGS, SETTING_TABLE_INSIDE),
    /**
     * 
     */
    SETTING_RESOURCE_IMPORT_PATH(TableType.SETTINGS, SETTING_RESOURCE_IMPORT),
    /**
     * 
     */
    SETTING_RESOURCE_UNWANTED_ARGUMENTS(TableType.SETTINGS,
            SETTING_RESOURCE_IMPORT_PATH),
    /**
     * 
     */
    SETTING_DOCUMENTATION(TableType.SETTINGS, SETTING_TABLE_INSIDE),
    /**
     * 
     */
    SETTING_DOCUMENTATION_TEXT(TableType.SETTINGS, SETTING_DOCUMENTATION),
    /**
     * 
     */
    SETTING_METADATA(TableType.SETTINGS, SETTING_TABLE_INSIDE),
    /**
     * 
     */
    SETTING_METADATA_KEY(TableType.SETTINGS, SETTING_METADATA),
    /**
     * 
     */
    SETTING_METADATA_VALUE(TableType.SETTINGS, SETTING_METADATA_KEY),
    /**
     * 
     */
    SETTING_SUITE_SETUP(TableType.SETTINGS, SETTING_TABLE_INSIDE),
    /**
     * 
     */
    SETTING_SUITE_SETUP_KEYWORD(TableType.SETTINGS, SETTING_SUITE_SETUP),
    /**
     *  
     */
    SETTING_SUITE_SETUP_KEYWORD_ARGUMENT(TableType.SETTINGS,
            SETTING_SUITE_SETUP_KEYWORD),
    /**
     * 
     */
    SETTING_SUITE_TEARDOWN(TableType.SETTINGS, SETTING_TABLE_INSIDE),
    /**
     * 
     */
    SETTING_SUITE_TEARDOWN_KEYWORD(TableType.SETTINGS, SETTING_SUITE_TEARDOWN),
    /**
     *  
     */
    SETTING_SUITE_TEARDOWN_KEYWORD_ARGUMENT(TableType.SETTINGS,
            SETTING_SUITE_TEARDOWN_KEYWORD),
    /**
     * 
     */
    SETTING_FORCE_TAGS(TableType.SETTINGS, SETTING_TABLE_INSIDE),
    /**
     * 
     */
    SETTING_FORCE_TAGS_TAG_NAME(TableType.SETTINGS, SETTING_FORCE_TAGS),
    /**
     * 
     */
    SETTING_DEFAULT_TAGS(TableType.SETTINGS, SETTING_TABLE_INSIDE),
    /**
     * 
     */
    SETTING_DEFAULT_TAGS_TAG_NAME(TableType.SETTINGS, SETTING_DEFAULT_TAGS),
    /**
     * 
     */
    SETTING_TEST_SETUP(TableType.SETTINGS, SETTING_TABLE_INSIDE),
    /**
     * 
     */
    SETTING_TEST_SETUP_KEYWORD(TableType.SETTINGS, SETTING_TEST_SETUP),
    /**
     *  
     */
    SETTING_TEST_SETUP_KEYWORD_ARGUMENT(TableType.SETTINGS,
            SETTING_TEST_SETUP_KEYWORD),
    /**
     * 
     */
    SETTING_TEST_TEARDOWN(TableType.SETTINGS, SETTING_TABLE_INSIDE),
    /**
     * 
     */
    SETTING_TEST_TEARDOWN_KEYWORD(TableType.SETTINGS, SETTING_TEST_TEARDOWN),
    /**
     *  
     */
    SETTING_TEST_TEARDOWN_KEYWORD_ARGUMENT(TableType.SETTINGS,
            SETTING_TEST_TEARDOWN_KEYWORD),
    /**
     * 
     */
    SETTING_TEST_TEMPLATE(TableType.SETTINGS, SETTING_TABLE_INSIDE),
    /**
     * 
     */
    SETTING_TEST_TEMPLATE_KEYWORD(TableType.SETTINGS, SETTING_TEST_TEMPLATE),
    /**
     * 
     */
    SETTING_TEST_TEMPLATE_KEYWORD_UNWANTED_ARGUMENTS(TableType.SETTINGS,
            SETTING_TEST_TEMPLATE_KEYWORD),
    /**
     * 
     */
    SETTING_TEST_TIMEOUT(TableType.SETTINGS, SETTING_TABLE_INSIDE),
    /**
     * 
     */
    SETTING_TEST_TIMEOUT_VALUE(TableType.SETTINGS, SETTING_TEST_TIMEOUT),
    /**
     * 
     */
    SETTING_TEST_TIMEOUT_MESSAGE_ARGUMENTS(TableType.SETTINGS,
            SETTING_TEST_TIMEOUT_VALUE),
    /**
     * 
     */
    SETTING_UNKNOWN(TableType.SETTINGS, SETTING_TABLE_INSIDE),
    /**
     * 
     */
    SETTING_UNKNOWN_TRASH_ELEMENT(TableType.SETTINGS, SETTING_UNKNOWN),
    /**
     * 
     */
    SCALAR_VARIABLE_DECLARATION(TableType.VARIABLES, VARIABLE_TABLE_INSIDE),
    /**
     * 
     */
    SCALAR_VARIABLE_VALUE(TableType.VARIABLES, SCALAR_VARIABLE_DECLARATION),
    /**
     * 
     */
    LIST_VARIABLE_DECLARATION(TableType.VARIABLES, VARIABLE_TABLE_INSIDE),
    /**
     * 
     */
    LIST_VARIABLE_VALUE(TableType.VARIABLES, LIST_VARIABLE_DECLARATION),
    /**
     * 
     */
    DICTIONARY_VARIABLE_DECLARATION(TableType.VARIABLES, VARIABLE_TABLE_INSIDE),
    /**
     * 
     */
    DICTIONARY_VARIABLE_VALUE(TableType.VARIABLES,
            DICTIONARY_VARIABLE_DECLARATION),
    /**
     * 
     */
    VARIABLE_UNKNOWN(TableType.VARIABLES, VARIABLE_TABLE_INSIDE),
    /**
     * 
     */
    VARIABLE_UNKNOWN_VALUE(TableType.VARIABLES, VARIABLE_UNKNOWN),
    /**
     * 
     */
    TEST_CASE_DECLARATION(TableType.TEST_CASE, TEST_CASE_TABLE_INSIDE),
    /**
     * 
     */
    TEST_CASE_EMPTY_LINE(TableType.TEST_CASE, TEST_CASE_DECLARATION),
    /**
     * 
     */
    TEST_CASE_INSIDE_ACTION(TableType.TEST_CASE, TEST_CASE_DECLARATION),
    /**
     * 
     */
    TEST_CASE_INSIDE_ACTION_ARGUMENT(TableType.TEST_CASE,
            TEST_CASE_INSIDE_ACTION),
    /**
     * 
     */
    TEST_CASE_SETTING_DOCUMENTATION_DECLARATION(TableType.TEST_CASE,
            TEST_CASE_DECLARATION),
    /**
     * 
     */
    TEST_CASE_SETTING_DOCUMENTATION_TEXT(TableType.TEST_CASE,
            TEST_CASE_SETTING_DOCUMENTATION_DECLARATION),
    /**
     * 
     */
    TEST_CASE_SETTING_SETUP(TableType.TEST_CASE, TEST_CASE_DECLARATION),
    /**
     * 
     */
    TEST_CASE_SETTING_SETUP_KEYWORD(TableType.TEST_CASE,
            TEST_CASE_SETTING_SETUP),
    /**
     *  
     */
    TEST_CASE_SETTING_SETUP_KEYWORD_ARGUMENT(TableType.TEST_CASE,
            TEST_CASE_SETTING_SETUP_KEYWORD),
    /**
     * 
     */
    TEST_CASE_SETTING_TAGS(TableType.TEST_CASE, TEST_CASE_DECLARATION),
    /**
     * 
     */
    TEST_CASE_SETTING_TAGS_TAG_NAME(TableType.TEST_CASE, TEST_CASE_SETTING_TAGS),
    /**
     * 
     */
    TEST_CASE_SETTING_TEARDOWN(TableType.TEST_CASE, TEST_CASE_DECLARATION),
    /**
     * 
     */
    TEST_CASE_SETTING_TEARDOWN_KEYWORD(TableType.TEST_CASE,
            TEST_CASE_SETTING_TEARDOWN),
    /**
     *  
     */
    TEST_CASE_SETTING_TEARDOWN_KEYWORD_ARGUMENT(TableType.TEST_CASE,
            TEST_CASE_SETTING_SETUP_KEYWORD),
    /**
     * 
     */
    TEST_CASE_SETTING_TEST_TEMPLATE(TableType.TEST_CASE, TEST_CASE_DECLARATION),
    /**
     * 
     */
    TEST_CASE_SETTING_TEST_TEMPLATE_KEYWORD(TableType.TEST_CASE,
            TEST_CASE_SETTING_TEST_TEMPLATE),
    /**
     * 
     */
    TEST_CASE_SETTING_TEST_TEMPLATE_KEYWORD_UNWANTED_ARGUMENTS(
            TableType.TEST_CASE, TEST_CASE_SETTING_TEST_TEMPLATE_KEYWORD),
    /**
     * 
     */
    TEST_CASE_SETTING_TEST_TIMEOUT(TableType.TEST_CASE, TEST_CASE_DECLARATION),
    /**
     * 
     */
    TEST_CASE_SETTING_TEST_TIMEOUT_VALUE(TableType.TEST_CASE,
            TEST_CASE_SETTING_TEST_TIMEOUT),
    /**
     * 
     */
    TEST_CASE_SETTING_TEST_TIMEOUT_MESSAGE_ARGUMENTS(TableType.TEST_CASE,
            TEST_CASE_SETTING_TEST_TIMEOUT_VALUE),
    /**
     * 
     */
    KEYWORD_DECLARATION(TableType.KEYWORD, KEYWORD_TABLE_INSIDE),
    /**
     * 
     */
    KEYWORD_EMPTY_LINE(TableType.KEYWORD, KEYWORD_DECLARATION),
    /**
     * 
     */
    KEYWORD_INSIDE_ACTION(TableType.KEYWORD, KEYWORD_DECLARATION),
    /**
     * 
     */
    KEYWORD_INSIDE_ACTION_ARGUMENT(TableType.KEYWORD, KEYWORD_INSIDE_ACTION),
    /**
     * 
     */
    KEYWORD_SETTING_DOCUMENTATION_DECLARATION(TableType.KEYWORD,
            KEYWORD_DECLARATION),
    /**
     * 
     */
    KEYWORD_SETTING_DOCUMENTATION_TEXT(TableType.KEYWORD,
            KEYWORD_SETTING_DOCUMENTATION_DECLARATION),
    /**
     * 
     */
    KEYWORD_SETTING_TAGS(TableType.KEYWORD, KEYWORD_DECLARATION),
    /**
     * 
     */
    KEYWORD_SETTING_TAGS_TAG_NAME(TableType.KEYWORD, KEYWORD_SETTING_TAGS),
    /**
     * 
     */
    KEYWORD_SETTING_ARGUMENTS(TableType.KEYWORD, KEYWORD_DECLARATION),
    /**
     * 
     */
    KEYWORD_SETTING_ARGUMENTS_ARGUMENT_VALUE(TableType.KEYWORD,
            KEYWORD_SETTING_ARGUMENTS),
    /**
     * 
     */
    KEYWORD_SETTING_RETURN(TableType.KEYWORD, KEYWORD_DECLARATION),
    /**
     * 
     */
    KEYWORD_SETTING_RETURN_VALUE(TableType.KEYWORD, KEYWORD_SETTING_RETURN),
    /**
     * 
     */
    KEYWORD_SETTING_TEARDOWN(TableType.KEYWORD, KEYWORD_DECLARATION),
    /**
     * 
     */
    KEYWORD_SETTING_TEARDOWN_KEYWORD(TableType.KEYWORD,
            KEYWORD_SETTING_TEARDOWN),
    /**
     *  
     */
    KEYWORD_SETTING_TEARDOWN_KEYWORD_ARGUMENT(TableType.KEYWORD,
            KEYWORD_SETTING_TEARDOWN_KEYWORD),
    /**
     * 
     */
    KEYWORD_SETTING_TIMEOUT(TableType.KEYWORD, KEYWORD_DECLARATION),
    /**
     * 
     */
    KEYWORD_SETTING_TIMEOUT_VALUE(TableType.KEYWORD, KEYWORD_SETTING_TIMEOUT),
    /**
     * 
     */
    KEYWORD_SETTING_TIMEOUT_MESSAGE_ARGUMENTS(TableType.KEYWORD,
            KEYWORD_SETTING_TIMEOUT_VALUE);

    private final TableType table;
    private final ParsingState previousState;
    private static final List<ParsingState> SETTINGS_STATUSES = new ArrayList<>();


    private ParsingState(final TableType type, final ParsingState previousState) {
        this.table = type;
        this.previousState = previousState;
    }


    public TableType getTable() {
        return table;
    }


    public ParsingState getPreviousState() {
        return previousState;
    }


    public static List<ParsingState> getSettingsStates() {
        if (SETTINGS_STATUSES.isEmpty()) {
            for (final ParsingState s : ParsingState.values()) {
                if (isSettingTableInside(s.previousState)) {
                    SETTINGS_STATUSES.add(s);
                }
            }
        }

        return SETTINGS_STATUSES;
    }


    private static boolean isSettingTableInside(final ParsingState state) {
        return (state == ParsingState.SETTING_TABLE_INSIDE
                || state == ParsingState.KEYWORD_DECLARATION
                || state == ParsingState.VARIABLE_TABLE_INSIDE || state == ParsingState.TEST_CASE_DECLARATION);
    }

    public enum TableType {
        SETTINGS, VARIABLES, KEYWORD, TEST_CASE;
    }
}
