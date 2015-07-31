package org.robotframework.ide.core.testData.text.read.recognizer;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.text.read.IRobotTokenType;


public enum RobotTokenType implements IRobotTokenType {
    /**
     * 
     */
    UNKNOWN(),
    /**
     * 
     */
    EMPTY_CELL(" \\ "),
    /**
     * 
     */
    SETTINGS_TABLE_HEADER("Setting", "Settings", "Metadata"),
    /**
     * 
     */
    VARIABLES_TABLE_HEADER("Variable", "Variables"),
    /**
     * 
     */
    TEST_CASES_TABLE_HEADER("Test Case", "Test Cases"),
    /**
    * 
    */
    KEYWORDS_TABLE_HEADER("Keyword", "Keywords", "User Keyword",
            "User Keywords"),
    /**
     * 
     */
    TABLE_HEADER_COLUMN,
    /**
     * 
     */
    START_HASH_COMMENT("#"),
    /**
     * 
     */
    COMMENT_CONTINUE,
    /**
     * 
     */
    PREVIOUS_LINE_CONTINUE("..."),
    /**
     * 
     */
    SETTING_LIBRARY_DECLARATION("Library", "Library:"),
    /**
     * 
     */
    SETTING_LIBRARY_NAME,
    /**
     * 
     */
    SETTING_LIBRARY_ARGUMENT,
    /**
     * 
     */
    SETTING_LIBRARY_ALIAS("WITH NAME"),
    /**
     * 
     */
    SETTING_LIBRARY_ALIAS_VALUE,
    /**
     * 
     */
    SETTING_VARIABLES_DECLARATION("Variables", "Variables:"),
    /**
     * 
     */
    SETTING_VARIABLES_FILE_NAME,
    /**
     * 
     */
    SETTING_VARIABLES_ARGUMENT,
    /**
     * 
     */
    SETTING_RESOURCE_DECLARATION("Resource", "Resource:"),
    /**
     * 
     */
    SETTING_RESOURCE_FILE_NAME,
    /**
     * 
     */
    SETTING_RESOURCE_UNWANTED_ARGUMENT,
    /**
     * 
     */
    SETTING_DOCUMENTATION_DECLARATION("Documentation", "Documentation:"),
    /**
     * 
     */
    SETTING_DOCUMENTATION_TEXT,
    /**
     * 
     */
    SETTING_METADATA_DECLARATION("Metadata", "Metadata:"),
    /**
     * 
     */
    SETTING_METADATA_KEY,
    /**
     * 
     */
    SETTING_METADATA_VALUE,
    /**
     * 
     */
    SETTING_SUITE_SETUP_DECLARATION("Suite Setup", "Suite Setup:",
            "Suite Precondition", "Suite Precondition:"),
    /**
     * 
     */
    SETTING_SUITE_SETUP_KEYWORD_NAME,
    /**
     * 
     */
    SETTING_SUITE_SETUP_KEYWORD_ARGUMENT,
    /**
     * 
     */
    SETTING_SUITE_TEARDOWN_DECLARATION("Suite Teardown", "Suite Teardown:",
            "Suite Postcondition", "Suite Postcondition:"),
    /**
     * 
     */
    SETTING_SUITE_TEARDOWN_KEYWORD_NAME,
    /**
     * 
     */
    SETTING_SUITE_TEARDOWN_KEYWORD_ARGUMENT,
    /**
     * 
     */
    SETTING_FORCE_TAGS_DECLARATION("Force Tags", "Force Tags:"),
    /**
     * 
     */
    SETTING_FORCE_TAG,
    /**
     * 
     */
    SETTING_DEFAULT_TAGS_DECLARATION("Default Tags", "Default Tags:"),
    /**
     * 
     */
    SETTING_DEFAULT_TAG;

    private final List<String> representationForNew = new LinkedList<>();


    public List<String> getRepresentation() {
        return representationForNew;
    }


    private RobotTokenType(String... representation) {
        representationForNew.addAll(Arrays.asList(representation));
    }
}