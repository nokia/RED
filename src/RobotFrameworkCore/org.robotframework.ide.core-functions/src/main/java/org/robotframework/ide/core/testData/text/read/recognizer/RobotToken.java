package org.robotframework.ide.core.testData.text.read.recognizer;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.text.read.IRobotLineElement;


public class RobotToken implements IRobotLineElement {

    public static final int NOT_SET = -1;
    private int lineNumber = NOT_SET;
    private int startColumn = NOT_SET;
    private StringBuilder text = new StringBuilder();
    private RobotTokenType type = RobotTokenType.UNKNOWN;


    public int getLineNumber() {
        return lineNumber;
    }


    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }


    public int getStartColumn() {
        return startColumn;
    }


    public void setStartColumn(int startColumn) {
        this.startColumn = startColumn;
    }


    public StringBuilder getText() {
        return text;
    }


    public void setText(StringBuilder text) {
        this.text = text;
    }


    public int getEndColumn() {
        return startColumn + text.length();
    }


    public RobotTokenType getType() {
        return type;
    }


    public void setType(RobotTokenType type) {
        this.type = type;
    }

    public static enum RobotTokenType {
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
                "User Keywords");

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
        return String.format(
                "RobotToken [lineNumber=%s, startColumn=%s, text=%s, type=%s]",
                lineNumber, startColumn, text, type);
    }

}
