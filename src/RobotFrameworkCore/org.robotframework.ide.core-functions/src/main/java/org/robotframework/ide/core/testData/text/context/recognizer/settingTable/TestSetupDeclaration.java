package org.robotframework.ide.core.testData.text.context.recognizer.settingTable;

import org.robotframework.ide.core.testData.text.lexer.RobotWordType;


public class TestSetupDeclaration extends ASettingTableElementRecognizer {

    public TestSetupDeclaration() {
        super(SettingTableRobotContextType.TABLE_SETTINGS_TEST_SETUP,
                createWithAllAsMandatory(RobotWordType.TEST_WORD,
                        RobotWordType.SETUP_WORD));
    }
}
