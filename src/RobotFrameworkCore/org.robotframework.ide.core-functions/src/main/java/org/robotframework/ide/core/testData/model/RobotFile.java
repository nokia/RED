package org.robotframework.ide.core.testData.model;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.table.KeywordTable;
import org.robotframework.ide.core.testData.model.table.SettingTable;
import org.robotframework.ide.core.testData.model.table.TestCaseTable;
import org.robotframework.ide.core.testData.model.table.VariableTable;
import org.robotframework.ide.core.testData.text.read.RobotLine;


public class RobotFile {

    private final SettingTable settingTable = new SettingTable();
    private final VariableTable variableTable = new VariableTable();
    private final TestCaseTable testCaseTable = new TestCaseTable();
    private final KeywordTable keywordTable = new KeywordTable();

    private final List<RobotLine> fileContent = new LinkedList<>();


    public List<RobotLine> getFileContent() {
        return fileContent;
    }


    public void addNewLine(final RobotLine line) {
        this.fileContent.add(line);
    }


    public SettingTable getSettingTable() {
        return settingTable;
    }


    public VariableTable getVariableTable() {
        return variableTable;
    }


    public TestCaseTable getTestCaseTable() {
        return testCaseTable;
    }


    public KeywordTable getKeywordTable() {
        return keywordTable;
    }

}
