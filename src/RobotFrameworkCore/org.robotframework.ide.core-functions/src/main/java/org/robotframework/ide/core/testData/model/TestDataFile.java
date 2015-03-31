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

    private final SettingTable settings = new SettingTable();
    private final VariablesTable variables = new VariablesTable();
    private final TestCaseTable testCases = new TestCaseTable();
    private final KeywordTable keywords = new KeywordTable();

    private TestDataType type = TestDataType.UNKNOWN_FILE;


    /**
     * @return
     */
    public TestDataType getTestDataFileType() {
        return this.type;
    }


    /**
     * @param type
     */
    public void setTestDataFileType(TestDataType type) {
        this.type = type;
    }

    public static enum TestDataType {
        TEST_SUITE_FILE, TEST_CASE_FILE, RESOURCES_FILE, DIRECTORY, UNKNOWN_FILE
    }
}
