package org.robotframework.ide.core.testData.model;

import org.robotframework.ide.core.testData.model.table.KeywordTable;
import org.robotframework.ide.core.testData.model.table.SettingTable;
import org.robotframework.ide.core.testData.model.table.TestCaseTable;
import org.robotframework.ide.core.testData.model.table.VariablesTable;


/**
 * Represents test file / directory, contains all possible settings and tables.
 * 
 * @author wypych
 * @serial RobotFramework 2.8.6
 * @serial 1.0
 * 
 */
public class TestDataFile {

    /**
     * Used by: Test Suites, Test Case files and Resources files. In Resources
     * files the settings table doesn't contains only Documentation, Libraries,
     * other Resources and Variables.
     */
    private SettingTable settings = new SettingTable();
    /**
     * Used by: Test Suites, Test Case files and Resources files
     */
    private VariablesTable variables = new VariablesTable();
    /**
     * Used by: Test Case files only
     */
    private TestCaseTable testCases = new TestCaseTable();
    /**
     * Used by: Test Suites, Test Case files and Resources files
     */
    private KeywordTable keywords = new KeywordTable();

    private TestDataType type = TestDataType.UNKNOWN_FILE;


    public SettingTable getSettings() {
        return settings;
    }


    public void setSettings(SettingTable settings) {
        this.settings = settings;
    }


    public VariablesTable getVariables() {
        return variables;
    }


    public void setVariables(VariablesTable variables) {
        this.variables = variables;
    }


    public TestCaseTable getTestCases() {
        return testCases;
    }


    public void setTestCases(TestCaseTable testCases) {
        this.testCases = testCases;
    }


    public KeywordTable getKeywords() {
        return keywords;
    }


    public void setKeywords(KeywordTable keywords) {
        this.keywords = keywords;
    }


    /**
     * @return say what type of data it is current mapped element
     */
    public TestDataType getTestDataFileType() {
        return this.type;
    }


    /**
     * @param type
     *            of data it is current mapped element
     */
    public void setTestDataFileType(TestDataType type) {
        this.type = type;
    }

    /**
     * Indicating what kind of data we have, because it is not so many
     * difference between each test data types, it is one common type contains
     * all possibilities.
     * 
     * @author wypych
     * @serial RobotFramework 2.8.6
     * @serial 1.0
     * 
     */
    public static enum TestDataType {
        /**
         * usually __init__.ext file where ext is supported file type extension
         */
        TEST_SUITE_FILE,
        /**
         * contains test cases
         */
        TEST_CASE_FILE,
        /**
         * resource shouldn't contains any test case
         */
        RESOURCES_FILE,
        /**
         * directory in model hierarchy structure
         */
        DIRECTORY,
        /**
         * during parsing problem with file occurred or file is not in any Robot
         * Framework accepted format
         */
        UNKNOWN_FILE
    }
}
