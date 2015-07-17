package org.robotframework.ide.core.testData.model;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.table.KeywordsTable;
import org.robotframework.ide.core.testData.model.table.SettingsTable;
import org.robotframework.ide.core.testData.model.table.TestCasesTable;
import org.robotframework.ide.core.testData.model.table.VariablesTable;


public class RobotTestDataFile {

    private SettingsTable settingsTable = new SettingsTable(null);
    private VariablesTable variablesTable = new VariablesTable(null);
    private TestCasesTable testCaseTable = new TestCasesTable(null);
    private KeywordsTable keywordsTable = new KeywordsTable(null);
    private boolean isInitializeFile;
    private final boolean isDirectory;

    private TestDataType type = TestDataType.UNKNOWN;
    private List<RobotLine> content = new LinkedList<>();


    public RobotTestDataFile() {
        this(false, false);
    }


    public static RobotTestDataFile createRobotFile() {
        return new RobotTestDataFile();
    }


    public static RobotTestDataFile createDirectory() {
        return new RobotTestDataFile(false, true);
    }


    public static RobotTestDataFile createInitializeFile() {
        return new RobotTestDataFile(true, false);
    }


    private RobotTestDataFile(final boolean isInitFile, final boolean isDir) {
        this.isInitializeFile = isInitFile;
        this.isDirectory = isDir;
    }


    public List<RobotLine> getContent() {
        return Collections.unmodifiableList(content);
    }


    public SettingsTable getSettingsTable() {
        return settingsTable;
    }


    public void setSettingsTable(SettingsTable settingsTable) {
        this.settingsTable = settingsTable;
        resolveFileType();
    }


    public VariablesTable getVariablesTable() {
        return variablesTable;
    }


    public void setVariablesTable(VariablesTable variablesTable) {
        this.variablesTable = variablesTable;
        resolveFileType();
    }


    public TestCasesTable getTestCaseTable() {
        return testCaseTable;
    }


    public void setTestCaseTable(TestCasesTable testCaseTable) {
        this.testCaseTable = testCaseTable;
        resolveFileType();
    }


    public KeywordsTable getKeywordsTable() {
        return keywordsTable;
    }


    public void setKeywordsTable(KeywordsTable keywordsTable) {
        this.keywordsTable = keywordsTable;
        resolveFileType();
    }


    public TestDataType getType() {
        return type;
    }


    public void setType(TestDataType type) {
        this.type = type;
    }


    public void resolveFileType() throws IllegalStateException {
        boolean settingsPresent = settingsTable.isPresent();
        boolean testCasePresent = testCaseTable.isPresent();
        boolean keywordsPresent = keywordsTable.isPresent();
        boolean variablePresent = variablesTable.isPresent();

        boolean isAnyTablePresent = (settingsPresent | testCasePresent
                | keywordsPresent | variablePresent);
        if (isAnyTablePresent) {
            if (testCasePresent) {
                if (isInitializeFile) {
                    // error: __init__ should not have any test case
                    throw new IllegalStateException(
                            "Initialization file can't have any *** Test Case *** table.");
                } else {
                    type = TestDataType.TEST_SUITE_FILE;
                }
            } else {
                if (isInitializeFile) {
                    type = TestDataType.TEST_SUITE_INITIALIZATION_FILE;
                } else {
                    type = TestDataType.RESOURCE_FILE;
                }
            }

            if (isDirectory) {
                throw new IllegalStateException(
                        "Directory can't have any kind of table.");
            }
        } else {
            if (isInitializeFile) {
                type = TestDataType.TEST_SUITE_INITIALIZATION_FILE;
            } else {
                type = TestDataType.RESOURCE_FILE;
            }

            if (isDirectory) {
                type = TestDataType.TEST_SUITE_DIRECTORY;
            }
        }
    }

    public static enum TestDataType {
        /**
         * initial state
         */
        UNKNOWN,
        /**
         * *.ext file with user defined keywords, variables
         */
        RESOURCE_FILE,
        /**
         * *.ext the same like {@link #RESOURCE_FILE} , but contains test cases
         */
        TEST_SUITE_FILE,
        /**
         * directory
         */
        TEST_SUITE_DIRECTORY,
        /**
         * __init__.ext file - has the same content like {@link #RESOURCE_FILE}
         */
        TEST_SUITE_INITIALIZATION_FILE
    }
}
