package org.robotframework.ide.core.testData.model;

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

    private TestDataType type = TestDataType.UNKNOWN;
    private List<RobotLine> linesInFile = new LinkedList<>();

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
