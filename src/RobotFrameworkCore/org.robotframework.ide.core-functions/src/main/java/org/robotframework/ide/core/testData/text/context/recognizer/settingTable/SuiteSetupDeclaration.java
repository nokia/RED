package org.robotframework.ide.core.testData.text.context.recognizer.settingTable;

import org.robotframework.ide.core.testData.text.lexer.RobotWordType;


public class SuiteSetupDeclaration extends ASettingTableElementRecognizer {

    public SuiteSetupDeclaration() {
        super(SettingTableRobotContextType.TABLE_SETTINGS_SUITE_SETUP,
                createWithAllAsMandatory(RobotWordType.SUITE_WORD,
                        RobotWordType.SETUP_WORD));
    }
}
