package org.robotframework.ide.core.testData.text.context.recognizer.settingTable;

import org.robotframework.ide.core.testData.text.lexer.RobotWordType;


public class SuitePostconditionDeclaration extends
        ASettingTableElementRecognizer {

    public SuitePostconditionDeclaration() {
        super(SettingTableRobotContextType.TABLE_SETTINGS_SUITE_POSTCONDTION,
                createWithAllAsMandatory(RobotWordType.SUITE_WORD,
                        RobotWordType.POSTCONDITION_WORD));
    }
}
