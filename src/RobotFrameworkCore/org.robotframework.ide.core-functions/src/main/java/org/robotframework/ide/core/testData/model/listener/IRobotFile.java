package org.robotframework.ide.core.testData.model.listener;

import org.robotframework.ide.core.testData.model.table.KeywordTable;
import org.robotframework.ide.core.testData.model.table.SettingTable;
import org.robotframework.ide.core.testData.model.table.TestCaseTable;
import org.robotframework.ide.core.testData.model.table.VariableTable;


public interface IRobotFile {

    SettingTable getSettingTable();


    VariableTable getVariableTable();


    TestCaseTable getTestCaseTable();


    KeywordTable getKeywordTable();

}
