package org.robotframework.ide.core.testData.text.context.recognizer.settingTable;

import org.robotframework.ide.core.testData.text.context.recognizer.ATableElementRecognizer;
import org.robotframework.ide.core.testData.text.lexer.RobotWordType;


public class SuitePostconditionDeclaration extends ATableElementRecognizer {

    public SuitePostconditionDeclaration() {
        super(SettingTableRobotContextType.TABLE_SETTINGS_SUITE_POSTCONDTION,
                createExpectedForSettingsTable(RobotWordType.SUITE_WORD,
                        RobotWordType.POSTCONDITION_WORD));
    }
}
