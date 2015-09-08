package org.robotframework.ide.core.testData.model.objectCreator;

import org.robotframework.ide.core.testData.model.IRobotFileOutput;
import org.robotframework.ide.core.testData.model.table.KeywordTable;
import org.robotframework.ide.core.testData.model.table.RobotExecutableRow;
import org.robotframework.ide.core.testData.model.table.SettingTable;
import org.robotframework.ide.core.testData.model.table.TableHeader;
import org.robotframework.ide.core.testData.model.table.TestCaseTable;
import org.robotframework.ide.core.testData.model.table.VariableTable;
import org.robotframework.ide.core.testData.text.read.RobotLine;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;


public interface IRobotModelObjectCreator extends
        ISettingTableRobotModelObjectCreator,
        IVariableTableRobotModelObjectCreator,
        ITestCaseTableRobotModelObjectCreator,
        IKeywordTableRobotModelObjectCreator {

    IRobotFileOutput createRobotFileOutput();


    SettingTable createSettingTable();


    VariableTable createVariableTable();


    TestCaseTable createTestCaseTable();


    KeywordTable createKeywordTable();


    TableHeader createTableHeader(final RobotToken tableHeaderToken);


    RobotExecutableRow createRobotExecutableRow();


    RobotLine createRobotLine(final int lineNumber);

}
