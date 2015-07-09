package org.robotframework.ide.core.testData.text.context.recognizer.settingTable;

import org.robotframework.ide.core.testData.text.context.recognizer.ATableElementRecognizer;
import org.robotframework.ide.core.testData.text.lexer.RobotWordType;


public class SuiteSetupDeclaration extends ATableElementRecognizer {

    public SuiteSetupDeclaration() {
        super(SettingTableRobotContextType.TABLE_SETTINGS_SUITE_SETUP,
                createExpectedForSettingsTable(RobotWordType.SUITE_WORD,
                        RobotWordType.SETUP_WORD));
    }
}
