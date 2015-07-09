package org.robotframework.ide.core.testData.text.context.recognizer.settingTable;

import org.robotframework.ide.core.testData.text.context.recognizer.ATableElementRecognizer;
import org.robotframework.ide.core.testData.text.lexer.RobotWordType;


public class TestTimeoutDeclaration extends ATableElementRecognizer {

    public TestTimeoutDeclaration() {
        super(SettingTableRobotContextType.TABLE_SETTINGS_TEST_TIMEOUT,
                createExpectedForSettingsTable(RobotWordType.TEST_WORD,
                        RobotWordType.TIMEOUT_WORD));
    }
}
