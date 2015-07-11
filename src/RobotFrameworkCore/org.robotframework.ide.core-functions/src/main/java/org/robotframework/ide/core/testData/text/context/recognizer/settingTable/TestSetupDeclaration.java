package org.robotframework.ide.core.testData.text.context.recognizer.settingTable;

import org.robotframework.ide.core.testData.text.context.recognizer.ATableElementRecognizer;
import org.robotframework.ide.core.testData.text.lexer.RobotWordType;


public class TestSetupDeclaration extends ATableElementRecognizer {

    public TestSetupDeclaration() {
        super(SettingTableRobotContextType.TABLE_SETTINGS_TEST_SETUP,
                createExpectedWithOptionalColonAsLast(RobotWordType.TEST_WORD,
                        RobotWordType.SETUP_WORD));
    }
}
