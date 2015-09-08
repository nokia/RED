package org.robotframework.ide.core.testData.model;

import java.util.List;

import org.robotframework.ide.core.testData.model.table.KeywordTable;
import org.robotframework.ide.core.testData.model.table.SettingTable;
import org.robotframework.ide.core.testData.model.table.TestCaseTable;
import org.robotframework.ide.core.testData.model.table.VariableTable;
import org.robotframework.ide.core.testData.text.read.RobotLine;


public interface IRobotFile {

    SettingTable getSettingTable();


    VariableTable getVariableTable();


    TestCaseTable getTestCaseTable();


    KeywordTable getKeywordTable();


    void addNewLine(final RobotLine line);


    List<RobotLine> getFileContent();


    boolean containsAnyRobotSection();

}
