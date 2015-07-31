package org.robotframework.ide.core.testData.text.read.recognizer;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.text.read.IRobotLineElement;
import org.robotframework.ide.core.testData.text.read.IRobotTokenType;


public class RobotToken implements IRobotLineElement {

    private int lineNumber = NOT_SET;
    private int startColumn = NOT_SET;
    private StringBuilder text = new StringBuilder();
    private List<IRobotTokenType> types = new LinkedList<>();


    public RobotToken() {
        types.add(RobotTokenType.UNKNOWN);
    }


    @Override
    public int getLineNumber() {
        return lineNumber;
    }


    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }


    @Override
    public int getStartColumn() {
        return startColumn;
    }


    public void setStartColumn(int startColumn) {
        this.startColumn = startColumn;
    }


    @Override
    public StringBuilder getText() {
        return text;
    }


    public void setText(StringBuilder text) {
        this.text = text;
    }


    @Override
    public int getEndColumn() {
        return startColumn + text.length();
    }


    @Override
    public List<IRobotTokenType> getTypes() {
        return types;
    }


    public void setType(final IRobotTokenType type) {
        types.clear();
        types.add(type);
    }

    public static enum RobotTokenType implements IRobotTokenType {
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
        SETTING_SUITE_SETUP_KEYWORD_ARGUMENT;

        private final List<String> representationForNew = new LinkedList<>();


        public List<String> getRepresentation() {
            return representationForNew;
        }


        private RobotTokenType(String... representation) {
            representationForNew.addAll(Arrays.asList(representation));
        }
    }


    @Override
    public String toString() {
        return String
                .format("RobotToken [lineNumber=%s, startColumn=%s, text=%s, types=%s]",
                        lineNumber, startColumn, text, types);
    }

}
