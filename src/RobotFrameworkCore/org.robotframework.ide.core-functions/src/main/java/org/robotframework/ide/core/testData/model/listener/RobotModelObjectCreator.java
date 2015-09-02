package org.robotframework.ide.core.testData.model.listener;

import org.robotframework.ide.core.testData.model.RobotFileOutput;
import org.robotframework.ide.core.testData.model.table.KeywordTable;
import org.robotframework.ide.core.testData.model.table.SettingTable;
import org.robotframework.ide.core.testData.model.table.TableHeader;
import org.robotframework.ide.core.testData.model.table.TestCaseTable;
import org.robotframework.ide.core.testData.model.table.VariableTable;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;


public class RobotModelObjectCreator implements IRobotModelObjectCreator {

    @Override
    public TableHeader createTableHeader(final RobotToken tableHeaderToken) {
        return new TableHeader(tableHeaderToken);
    }


    @Override
    public KeywordTable createKeywordTable(final String uuid) {
        return new KeywordTable(uuid);
    }


    @Override
    public TestCaseTable createTestCaseTable(final String uuid) {
        return new TestCaseTable(uuid);
    }


    @Override
    public VariableTable createVariableTable(final String uuid) {
        return new VariableTable(uuid);
    }


    @Override
    public SettingTable createSettingTable(final String uuid) {
        return new SettingTable(uuid);
    }


    @Override
    public RobotFileOutput createRobotFileOutput() {
        RobotFileOutput rfo = new RobotFileOutput(this);

        return rfo;
    }


    public static RobotModelObjectCreator newInstance() {
        return new RobotModelObjectCreator();
    }


    private RobotModelObjectCreator() {
    }
}
