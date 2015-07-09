package org.robotframework.ide.core.testData.text.context.recognizer.settingTable;

import org.robotframework.ide.core.testData.text.context.recognizer.ATableElementRecognizer;
import org.robotframework.ide.core.testData.text.lexer.RobotWordType;


public class SuitePreconditionDeclaration extends ATableElementRecognizer {

    public SuitePreconditionDeclaration() {
        super(SettingTableRobotContextType.TABLE_SETTINGS_SUITE_PRECONDITION,
                createExpectedForSettingsTable(RobotWordType.SUITE_WORD,
                        RobotWordType.PRECONDITION_WORD));
    }
}
