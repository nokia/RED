package org.robotframework.ide.core.testData.model.listener;

import org.robotframework.ide.core.testData.model.RobotFileOutput;
import org.robotframework.ide.core.testData.model.table.KeywordTable;
import org.robotframework.ide.core.testData.model.table.SettingTable;
import org.robotframework.ide.core.testData.model.table.TableHeader;
import org.robotframework.ide.core.testData.model.table.TestCaseTable;
import org.robotframework.ide.core.testData.model.table.VariableTable;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;


public interface IRobotModelObjectCreator {

    RobotFileOutput createRobotFileOutput();


    SettingTable createSettingTable(final String uuid);


    VariableTable createVariableTable(final String uuid);


    TestCaseTable createTestCaseTable(final String uuid);


    KeywordTable createKeywordTable(final String uuid);


    TableHeader createTableHeader(final RobotToken tableHeaderToken);

}
