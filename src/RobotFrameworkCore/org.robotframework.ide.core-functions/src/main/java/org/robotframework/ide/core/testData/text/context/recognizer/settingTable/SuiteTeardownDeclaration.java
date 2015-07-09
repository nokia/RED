package org.robotframework.ide.core.testData.text.context.recognizer.settingTable;

import org.robotframework.ide.core.testData.text.context.recognizer.ATableElementRecognizer;
import org.robotframework.ide.core.testData.text.lexer.RobotWordType;


public class SuiteTeardownDeclaration extends ATableElementRecognizer {

    public SuiteTeardownDeclaration() {
        super(SettingTableRobotContextType.TABLE_SETTINGS_SUITE_TEARDOWN,
                createExpectedForSettingsTable(RobotWordType.SUITE_WORD,
                        RobotWordType.TEARDOWN_WORD));
    }
}
