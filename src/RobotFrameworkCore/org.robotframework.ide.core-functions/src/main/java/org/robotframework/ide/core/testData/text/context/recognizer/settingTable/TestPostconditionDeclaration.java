package org.robotframework.ide.core.testData.text.context.recognizer.settingTable;

import org.robotframework.ide.core.testData.text.context.recognizer.ATableElementRecognizer;
import org.robotframework.ide.core.testData.text.lexer.RobotWordType;


public class TestPostconditionDeclaration extends ATableElementRecognizer {

    public TestPostconditionDeclaration() {
        super(SettingTableRobotContextType.TABLE_SETTINGS_TEST_POSTCONDITION,
                createExpectedForSettingsTable(RobotWordType.TEST_WORD,
                        RobotWordType.POSTCONDITION_WORD));
    }
}
