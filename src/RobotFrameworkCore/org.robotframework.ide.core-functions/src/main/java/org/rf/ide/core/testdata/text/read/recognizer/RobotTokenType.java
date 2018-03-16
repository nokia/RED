/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.read.recognizer;

import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.testdata.model.RobotVersion;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.VersionAvailabilityInfo;
import org.rf.ide.core.testdata.text.read.VersionAvailabilityInfo.VersionAvailabilityInfoBuilder;

import com.google.common.collect.ArrayListMultimap;

public enum RobotTokenType implements IRobotTokenType {
    /**
     */
    UNKNOWN(TableType.NOT_STRICTLY_BELONGS),
    /**
     */
    VARIABLE_USAGE(TableType.NOT_STRICTLY_BELONGS),
    /**
     */
    ASSIGNMENT(TableType.NOT_STRICTLY_BELONGS, VersionAvailabilityInfoBuilder.create().addRepresentation("=").build()),
    /**
     */
    PRETTY_ALIGN_SPACE(
            TableType.NOT_STRICTLY_BELONGS,
            VersionAvailabilityInfoBuilder.create().addRepresentation(" ").build()),
    /**
     */
    USER_OWN_TABLE_HEADER(TableType.NOT_STRICTLY_BELONGS),
    /**
     */
    SETTINGS_TABLE_HEADER(
            TableType.SETTINGS,
            VersionAvailabilityInfoBuilder.create().addRepresentation("Setting").build(),
            VersionAvailabilityInfoBuilder.create().addRepresentation("Settings").build(),
            VersionAvailabilityInfoBuilder.create().addRepresentation("Metadata").deprecatedFrom("3.0").build()),
    /**
     */
    VARIABLES_TABLE_HEADER(
            TableType.VARIABLES,
            VersionAvailabilityInfoBuilder.create().addRepresentation("Variable").build(),
            VersionAvailabilityInfoBuilder.create().addRepresentation("Variables").build()),
    /**
     */
    TEST_CASES_TABLE_HEADER(
            TableType.TEST_CASES,
            VersionAvailabilityInfoBuilder.create().addRepresentation("Test Case").build(),
            VersionAvailabilityInfoBuilder.create().addRepresentation("Test Cases").build()),
    /**
     */
    KEYWORDS_TABLE_HEADER(
            TableType.KEYWORDS,
            VersionAvailabilityInfoBuilder.create().addRepresentation("Keyword").build(),
            VersionAvailabilityInfoBuilder.create().addRepresentation("Keywords").build(),
            VersionAvailabilityInfoBuilder.create().addRepresentation("User Keyword").deprecatedFrom("3.0").build(),
            VersionAvailabilityInfoBuilder.create().addRepresentation("User Keywords").build()),
    /**
     */
    TABLE_HEADER_COLUMN(TableType.NOT_STRICTLY_BELONGS),
    /**
     */
    START_HASH_COMMENT(
            TableType.NOT_STRICTLY_BELONGS,
            VersionAvailabilityInfoBuilder.create().addRepresentation("#").build()),
    /**
     */
    COMMENT_CONTINUE(TableType.NOT_STRICTLY_BELONGS),
    /**
     */
    PREVIOUS_LINE_CONTINUE(
            TableType.NOT_STRICTLY_BELONGS,
            VersionAvailabilityInfoBuilder.create().addRepresentation("...").build()),
    /**
     */
    SETTING_LIBRARY_DECLARATION(
            TableType.SETTINGS,
            true,
            VersionAvailabilityInfoBuilder.create().addRepresentation("Library").build(),
            VersionAvailabilityInfoBuilder.create().addRepresentation("Library:").build()),
    /**
     */
    SETTING_UNKNOWN(TableType.SETTINGS, true),
    /**
     */
    SETTING_UNKNOWN_ARGUMENT(TableType.SETTINGS),
    /**
     */
    SETTING_LIBRARY_NAME(TableType.SETTINGS),
    /**
     */
    SETTING_LIBRARY_ARGUMENT(TableType.SETTINGS),
    /**
     */
    SETTING_LIBRARY_ALIAS(
            TableType.SETTINGS,
            VersionAvailabilityInfoBuilder.create().addRepresentation("WITH NAME").build()),
    /**
     */
    SETTING_LIBRARY_ALIAS_VALUE(TableType.SETTINGS),
    /**
     */
    SETTING_VARIABLES_DECLARATION(
            TableType.SETTINGS,
            true,
            VersionAvailabilityInfoBuilder.create().addRepresentation("Variables").build(),
            VersionAvailabilityInfoBuilder.create().addRepresentation("Variables:").build()),
    /**
     */
    SETTING_VARIABLES_FILE_NAME(TableType.SETTINGS),
    /**
     */
    SETTING_VARIABLES_ARGUMENT(TableType.SETTINGS),
    /**
     */
    SETTING_RESOURCE_DECLARATION(
            TableType.SETTINGS,
            true,
            VersionAvailabilityInfoBuilder.create().addRepresentation("Resource").build(),
            VersionAvailabilityInfoBuilder.create().addRepresentation("Resource:").build()),
    /**
     */
    SETTING_RESOURCE_FILE_NAME(TableType.SETTINGS),
    /**
     */
    SETTING_RESOURCE_UNWANTED_ARGUMENT(TableType.SETTINGS),
    /**
     */
    SETTING_DOCUMENTATION_DECLARATION(
            TableType.SETTINGS,
            true,
            VersionAvailabilityInfoBuilder.create().addRepresentation("Documentation").build(),
            VersionAvailabilityInfoBuilder.create().addRepresentation("Documentation:").build(),
            VersionAvailabilityInfoBuilder.create().deprecatedFrom("3.0").addRepresentation("Document").build(),
            VersionAvailabilityInfoBuilder.create().deprecatedFrom("3.0").addRepresentation("Document:").build()),
    /**
     */
    SETTING_DOCUMENTATION_TEXT(TableType.SETTINGS),
    /**
     */
    SETTING_METADATA_DECLARATION(
            TableType.SETTINGS,
            true,
            VersionAvailabilityInfoBuilder.create().addRepresentation("Metadata").build(),
            VersionAvailabilityInfoBuilder.create().addRepresentation("Metadata:").build(),
            VersionAvailabilityInfoBuilder.create().deprecatedFrom("3.0").addRepresentation("Meta").build(),
            VersionAvailabilityInfoBuilder.create().deprecatedFrom("3.0").addRepresentation("Meta:").build()),
    /**
     */
    SETTING_METADATA_KEY(TableType.SETTINGS),
    /**
     */
    SETTING_METADATA_VALUE(TableType.SETTINGS),
    /**
     */
    SETTING_SUITE_SETUP_DECLARATION(
            TableType.SETTINGS,
            true,
            VersionAvailabilityInfoBuilder.create().addRepresentation("Suite Setup").build(),
            VersionAvailabilityInfoBuilder.create().addRepresentation("Suite Setup:").build(),
            VersionAvailabilityInfoBuilder.create()
                    .addRepresentation("Suite Precondition")
                    .deprecatedFrom("3.0")
                    .build(),
            VersionAvailabilityInfoBuilder.create()
                    .addRepresentation("Suite Precondition:")
                    .deprecatedFrom("3.0")
                    .build()),
    /**
     */
    SETTING_SUITE_SETUP_KEYWORD_NAME(TableType.SETTINGS),
    /**
     */
    SETTING_SUITE_SETUP_KEYWORD_ARGUMENT(TableType.SETTINGS),
    /**
     */
    SETTING_SUITE_TEARDOWN_DECLARATION(
            TableType.SETTINGS,
            true,
            VersionAvailabilityInfoBuilder.create().addRepresentation("Suite Teardown").build(),
            VersionAvailabilityInfoBuilder.create().addRepresentation("Suite Teardown:").build(),
            VersionAvailabilityInfoBuilder.create()
                    .addRepresentation("Suite Postcondition")
                    .deprecatedFrom("3.0")
                    .build(),
            VersionAvailabilityInfoBuilder.create()
                    .addRepresentation("Suite Postcondition:")
                    .deprecatedFrom("3.0")
                    .build()),
    /**
     */
    SETTING_SUITE_TEARDOWN_KEYWORD_NAME(TableType.SETTINGS),
    /**
     */
    SETTING_SUITE_TEARDOWN_KEYWORD_ARGUMENT(TableType.SETTINGS),
    /**
     */
    SETTING_FORCE_TAGS_DECLARATION(
            TableType.SETTINGS,
            true,
            VersionAvailabilityInfoBuilder.create().addRepresentation("Force Tags").build(),
            VersionAvailabilityInfoBuilder.create().addRepresentation("Force Tags:").build()),
    /**
     */
    SETTING_FORCE_TAG(TableType.SETTINGS),
    /**
     */
    SETTING_DEFAULT_TAGS_DECLARATION(
            TableType.SETTINGS,
            true,
            VersionAvailabilityInfoBuilder.create().addRepresentation("Default Tags").build(),
            VersionAvailabilityInfoBuilder.create().addRepresentation("Default Tags:").build()),
    /**
     */
    SETTING_DEFAULT_TAG(TableType.SETTINGS),
    /**
     */
    SETTING_TEST_SETUP_DECLARATION(
            TableType.SETTINGS,
            true,
            VersionAvailabilityInfoBuilder.create().addRepresentation("Test Setup").build(),
            VersionAvailabilityInfoBuilder.create().addRepresentation("Test Setup:").build(),
            VersionAvailabilityInfoBuilder.create()
                    .addRepresentation("Test Precondition")
                    .deprecatedFrom("3.0")
                    .build(),
            VersionAvailabilityInfoBuilder.create()
                    .addRepresentation("Test Precondition:")
                    .deprecatedFrom("3.0")
                    .build()),
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
    SETTING_TEST_TEARDOWN_DECLARATION(
            TableType.SETTINGS,
            true,
            VersionAvailabilityInfoBuilder.create().addRepresentation("Test Teardown").build(),
            VersionAvailabilityInfoBuilder.create().addRepresentation("Test Teardown:").build(),
            VersionAvailabilityInfoBuilder.create()
                    .addRepresentation("Test Postcondition")
                    .deprecatedFrom("3.0")
                    .build(),
            VersionAvailabilityInfoBuilder.create()
                    .addRepresentation("Test Postcondition:")
                    .deprecatedFrom("3.0")
                    .build()),
    /**
     */
    SETTING_TEST_TEARDOWN_KEYWORD_NAME(TableType.SETTINGS),
    /**
     */
    SETTING_TEST_TEARDOWN_KEYWORD_ARGUMENT(TableType.SETTINGS),
    /**
     */
    SETTING_TEST_TEMPLATE_DECLARATION(
            TableType.SETTINGS,
            true,
            VersionAvailabilityInfoBuilder.create().addRepresentation("Test Template").build(),
            VersionAvailabilityInfoBuilder.create().addRepresentation("Test Template:").build()),
    /**
     */
    SETTING_TEST_TEMPLATE_KEYWORD_NAME(TableType.SETTINGS),
    /**
     */
    SETTING_TEST_TEMPLATE_KEYWORD_UNWANTED_ARGUMENT(TableType.SETTINGS),
    /**
     */
    SETTING_TEST_TIMEOUT_DECLARATION(
            TableType.SETTINGS,
            true,
            VersionAvailabilityInfoBuilder.create().addRepresentation("Test Timeout").build(),
            VersionAvailabilityInfoBuilder.create().addRepresentation("Test Timeout:").build()),
    /**
     */
    SETTING_TEST_TIMEOUT_VALUE(TableType.SETTINGS),
    /**
     */
    SETTING_TEST_TIMEOUT_MESSAGE(TableType.SETTINGS),
    /**
     */
    VARIABLES_WRONG_DEFINED(TableType.VARIABLES, true),
    /**
     */
    VARIABLES_SCALAR_DECLARATION(TableType.VARIABLES, true),
    /**
     */
    VARIABLES_SCALAR_AS_LIST_DECLARATION(TableType.VARIABLES, true),
    /**
     */
    VARIABLES_LIST_DECLARATION(TableType.VARIABLES, true),
    /**
     */
    VARIABLES_DICTIONARY_DECLARATION(
            TableType.VARIABLES,
            true,
            VersionAvailabilityInfoBuilder.create().availableFrom("2.9").build()),
    /**
     */
    VARIABLES_ENVIRONMENT_DECLARATION(TableType.VARIABLES, true),
    /**
     */
    VARIABLES_UNKNOWN_DECLARATION(TableType.VARIABLES, true),
    /**
     */
    VARIABLES_VARIABLE_VALUE(TableType.VARIABLES),
    /**
     */
    VARIABLES_DICTIONARY_KEY(TableType.VARIABLES, VersionAvailabilityInfoBuilder.create().availableFrom("2.9").build()),
    /**
     */
    VARIABLES_DICTIONARY_VALUE(
            TableType.VARIABLES,
            VersionAvailabilityInfoBuilder.create().availableFrom("2.9").build()),
    /**
     */
    TEST_CASE_EMPTY_CELL(
            TableType.NOT_STRICTLY_BELONGS,
            VersionAvailabilityInfoBuilder.create().addRepresentation(" \\ ").build()),
    /**
     */
    TEST_CASE_SETTING_UNKNOWN_DECLARATION(TableType.TEST_CASES, true),
    /**
     */
    TEST_CASE_SETTING_UNKNOWN_ARGUMENTS(TableType.TEST_CASES),
    /**
     */
    TEST_CASE_SETTING_DOCUMENTATION(
            TableType.TEST_CASES,
            true,
            VersionAvailabilityInfoBuilder.create().addRepresentation("[Documentation]").build(),
            VersionAvailabilityInfoBuilder.create().addRepresentation("[Document]").deprecatedFrom("3.0").build()),
    /**
     */
    TEST_CASE_SETTING_DOCUMENTATION_TEXT(TableType.TEST_CASES),
    /**
     */
    TEST_CASE_SETTING_TAGS_DECLARATION(
            TableType.TEST_CASES,
            true,
            VersionAvailabilityInfoBuilder.create().addRepresentation("[Tags]").build()),
    /**
     */
    TEST_CASE_SETTING_TAGS(TableType.TEST_CASES),
    /**
     */
    TEST_CASE_SETTING_SETUP(
            TableType.TEST_CASES,
            true,
            VersionAvailabilityInfoBuilder.create().addRepresentation("[Setup]").build(),
            VersionAvailabilityInfoBuilder.create().addRepresentation("[Precondition]").deprecatedFrom("3.0").build()),
    /**
     */
    TEST_CASE_SETTING_SETUP_KEYWORD_NAME(TableType.TEST_CASES),
    /**
     */
    TEST_CASE_SETTING_SETUP_KEYWORD_ARGUMENT(TableType.TEST_CASES),
    /**
     */
    TEST_CASE_SETTING_TEARDOWN(
            TableType.TEST_CASES,
            true,
            VersionAvailabilityInfoBuilder.create().addRepresentation("[Teardown]").build(),
            VersionAvailabilityInfoBuilder.create().addRepresentation("[Postcondition]").deprecatedFrom("3.0").build()),
    /**
     */
    TEST_CASE_SETTING_TEARDOWN_KEYWORD_NAME(TableType.TEST_CASES),
    /**
     */
    TEST_CASE_SETTING_TEARDOWN_KEYWORD_ARGUMENT(TableType.TEST_CASES),
    /**
     */
    TEST_CASE_SETTING_TEMPLATE(
            TableType.TEST_CASES,
            true,
            VersionAvailabilityInfoBuilder.create().addRepresentation("[Template]").build()),
    /**
     */
    TEST_CASE_SETTING_TEMPLATE_KEYWORD_NAME(TableType.TEST_CASES),
    /**
     */
    TEST_CASE_SETTING_TEMPLATE_KEYWORD_UNWANTED_ARGUMENT(TableType.TEST_CASES),
    /**
     */
    TEST_CASE_SETTING_TIMEOUT(
            TableType.TEST_CASES,
            true,
            VersionAvailabilityInfoBuilder.create().addRepresentation("[Timeout]").build()),
    /**
     */
    TEST_CASE_SETTING_TIMEOUT_VALUE(TableType.TEST_CASES),
    /**
     */
    TEST_CASE_SETTING_TIMEOUT_MESSAGE(TableType.TEST_CASES),
    /**
     */
    TEST_CASE_NAME(TableType.TEST_CASES),
    /**
     */
    TEST_CASE_ACTION_NAME(TableType.TEST_CASES),
    /**
     */
    TEST_CASE_ACTION_ARGUMENT(TableType.TEST_CASES),
    /**
     */
    TEST_CASE_EMPTY_LINE(TableType.TEST_CASES),
    /**
     */
    KEYWORD_EMPTY_CELL(
            TableType.NOT_STRICTLY_BELONGS,
            VersionAvailabilityInfoBuilder.create().addRepresentation(" \\ ").build()),
    /**
     */
    KEYWORD_SETTING_UNKNOWN_DECLARATION(TableType.KEYWORDS, true),
    /**
     */
    KEYWORD_SETTING_UNKNOWN_ARGUMENTS(TableType.KEYWORDS),
    /**
     */
    KEYWORD_SETTING_DOCUMENTATION(
            TableType.KEYWORDS,
            true,
            VersionAvailabilityInfoBuilder.create().addRepresentation("[Documentation]").build(),
            VersionAvailabilityInfoBuilder.create().addRepresentation("[Document]").deprecatedFrom("3.0").build()),
    /**
     */
    KEYWORD_SETTING_DOCUMENTATION_TEXT(TableType.KEYWORDS),
    /**
     */
    KEYWORD_SETTING_TAGS(
            TableType.KEYWORDS,
            true,
            VersionAvailabilityInfoBuilder.create().addRepresentation("[Tags]").build()),
    /**
     */
    KEYWORD_SETTING_TAGS_TAG_NAME(TableType.KEYWORDS),
    /**
     */
    KEYWORD_SETTING_ARGUMENTS(
            TableType.KEYWORDS,
            true,
            VersionAvailabilityInfoBuilder.create().addRepresentation("[Arguments]").build()),
    /**
     */
    KEYWORD_SETTING_ARGUMENT(TableType.KEYWORDS),
    /**
     */
    KEYWORD_SETTING_RETURN(
            TableType.KEYWORDS,
            true,
            VersionAvailabilityInfoBuilder.create().addRepresentation("[Return]").build()),
    /**
     */
    KEYWORD_SETTING_RETURN_VALUE(TableType.KEYWORDS),
    /**
     * 
     */
    KEYWORD_SETTING_TEARDOWN(
            TableType.KEYWORDS,
            true,
            VersionAvailabilityInfoBuilder.create().addRepresentation("[Teardown]").build(),
            VersionAvailabilityInfoBuilder.create().addRepresentation("[Postcondition]").deprecatedFrom("3.0").build()),
    /**
     */
    KEYWORD_SETTING_TEARDOWN_KEYWORD_NAME(TableType.KEYWORDS),
    /**
     */
    KEYWORD_SETTING_TEARDOWN_KEYWORD_ARGUMENT(TableType.KEYWORDS),

    /**
     */
    KEYWORD_SETTING_TIMEOUT(
            TableType.KEYWORDS,
            true,
            VersionAvailabilityInfoBuilder.create().addRepresentation("[Timeout]").build()),
    /**
     */
    KEYWORD_SETTING_TIMEOUT_VALUE(TableType.KEYWORDS),
    /**
     */
    KEYWORD_SETTING_TIMEOUT_MESSAGE(TableType.KEYWORDS),
    /**
     */
    KEYWORD_NAME(TableType.KEYWORDS),
    /**
     */
    KEYWORD_ACTION_NAME(TableType.KEYWORDS),
    /**
     */
    KEYWORD_ACTION_ARGUMENT(TableType.KEYWORDS),
    /**
     */
    KEYWORD_EMPTY_LINE(TableType.KEYWORDS),
    /**
     */
    FOR_TOKEN(
            TableType.NOT_STRICTLY_BELONGS,
            VersionAvailabilityInfoBuilder.create().addRepresentation(": FOR").build(),
            VersionAvailabilityInfoBuilder.create().addRepresentation(":FOR").build()),
    /**
     */
    IN_TOKEN(
            TableType.NOT_STRICTLY_BELONGS,
            VersionAvailabilityInfoBuilder.create().addRepresentation("IN").build(),
            VersionAvailabilityInfoBuilder.create().addRepresentation("IN RANGE").build(),
            VersionAvailabilityInfoBuilder.create().addRepresentation("IN ENUMERATE").availableFrom("2.9").build(),
            VersionAvailabilityInfoBuilder.create().addRepresentation("IN ZIP").availableFrom("2.9").build()),
    /**
     */
    FOR_CONTINUE_TOKEN(
            TableType.NOT_STRICTLY_BELONGS,
            VersionAvailabilityInfoBuilder.create().addRepresentation("\\").build()), FOR_CONTINUE_ARTIFICIAL_TOKEN(
                    TableType.NOT_STRICTLY_BELONGS);

    private final List<String> text = new ArrayList<>(0);

    private final List<VersionAvailabilityInfo> representation = new ArrayList<>(0);

    private final TableType type;

    private final boolean settingDeclaration;

    private static final ArrayListMultimap<TableType, RobotTokenType> TYPE_TO_TABLE = ArrayListMultimap.create();

    static {
        final RobotTokenType[] values = RobotTokenType.values();
        for (final RobotTokenType type : values) {
            TYPE_TO_TABLE.put(type.type, type);
        }
    }

    @Override
    public List<String> getRepresentation() {
        return text;
    }

    private RobotTokenType(final TableType type, final VersionAvailabilityInfo... representations) {
        this(type, false, representations);
    }

    private RobotTokenType(final TableType type, final boolean settingDeclaration,
            final VersionAvailabilityInfo... representations) {
        this.type = type;
        this.settingDeclaration = settingDeclaration;
        for (final VersionAvailabilityInfo vInfo : representations) {
            representation.add(vInfo);
            text.add(vInfo.getRepresentation());
        }
    }

    public boolean isSettingDeclaration() {
        return settingDeclaration;
    }

    private enum TableType {
        NOT_STRICTLY_BELONGS, SETTINGS, VARIABLES, TEST_CASES, KEYWORDS;
    }

    public static boolean isTableHeader(final IRobotTokenType type) {
        return (type == SETTINGS_TABLE_HEADER || type == VARIABLES_TABLE_HEADER || type == TEST_CASES_TABLE_HEADER
                || type == KEYWORDS_TABLE_HEADER || type == USER_OWN_TABLE_HEADER);
    }

    public static List<RobotTokenType> getTypesForSettingsTable() {
        return getTypes(TableType.SETTINGS);
    }

    public static List<RobotTokenType> getTypesForVariablesTable() {
        return getTypes(TableType.VARIABLES);
    }

    public static List<RobotTokenType> getTypesForTestCasesTable() {
        return getTypes(TableType.TEST_CASES);
    }

    public static List<RobotTokenType> getTypesForKeywordsTable() {
        return getTypes(TableType.KEYWORDS);
    }

    public static List<RobotTokenType> getTypesNotStrictlyBelongs() {
        return getTypes(TableType.NOT_STRICTLY_BELONGS);
    }

    private static List<RobotTokenType> getTypes(final TableType type) {
        return new ArrayList<>(TYPE_TO_TABLE.get(type));
    }

    public static RobotTokenType findTypeOfDeclarationForKeywordSettingTable(final String textSettingDeclaration) {
        return findTypeOfDeclaration(getTypesForKeywordsTable(), textSettingDeclaration);
    }

    public static RobotTokenType findTypeOfDeclarationForTestCaseSettingTable(final String textSettingDeclaration) {
        return findTypeOfDeclaration(getTypesForTestCasesTable(), textSettingDeclaration);
    }

    public static RobotTokenType findTypeOfDeclarationForSettingTable(final String textSettingDeclaration) {
        return findTypeOfDeclaration(getTypesForSettingsTable(), textSettingDeclaration);
    }

    private static RobotTokenType findTypeOfDeclaration(final List<RobotTokenType> availableTypes,
            final String textSettingDeclaration) {

        for (final RobotTokenType type : availableTypes) {
            if (type.isSettingDeclaration()) {
                for (final String representation : type.getRepresentation()) {
                    if (representation.equalsIgnoreCase(textSettingDeclaration.trim())) {
                        return type;
                    }
                }
            }
        }

        return RobotTokenType.UNKNOWN;
    }

    @Override
    public List<VersionAvailabilityInfo> getVersionAvailabilityInfos() {
        return representation;
    }

    @Override
    public VersionAvailabilityInfo findVersionAvailabilityInfo(final String text) {
        VersionAvailabilityInfo vaiResult = null;
        for (final VersionAvailabilityInfo vInfo : representation) {
            if (vInfo.getRepresentation().equalsIgnoreCase(text)) {
                vaiResult = vInfo;
                break;
            }
        }
        return vaiResult;
    }

    public VersionAvailabilityInfo getTheMostCorrectOneRepresentation(final RobotVersion robotVersionInstalled) {
        VersionAvailabilityInfo vaiInCaseNoMatches = null;
        for (final VersionAvailabilityInfo vai : getVersionAvailabilityInfos()) {
            if (vai.getRepresentation() == null) {
                continue;
            }
            if ((vai.getAvailableFrom() == null || robotVersionInstalled.isNewerOrEqualTo(vai.getAvailableFrom()))
                    && vai.getDepracatedFrom() == null && vai.getRemovedFrom() == null) {
                vaiInCaseNoMatches = vai;
                break;
            } else {
                if (vaiInCaseNoMatches == null) {
                    vaiInCaseNoMatches = vai;
                    continue;
                }

                if (vai.getAvailableFrom() == null || robotVersionInstalled.isNewerOrEqualTo(vai.getAvailableFrom())) {
                    if (vai.getRemovedFrom() == null) {
                        if (vaiInCaseNoMatches.getDepracatedFrom() != null
                                && vai.getDepracatedFrom().isNewerThan(vaiInCaseNoMatches.getDepracatedFrom())) {
                            vaiInCaseNoMatches = vai;
                        }
                    } else {
                        if (vaiInCaseNoMatches.getRemovedFrom() != null
                                && vai.getRemovedFrom().isNewerThan(vaiInCaseNoMatches.getRemovedFrom())) {
                            vaiInCaseNoMatches = vai;
                        }
                    }
                }
            }
        }

        return vaiInCaseNoMatches;
    }
}
