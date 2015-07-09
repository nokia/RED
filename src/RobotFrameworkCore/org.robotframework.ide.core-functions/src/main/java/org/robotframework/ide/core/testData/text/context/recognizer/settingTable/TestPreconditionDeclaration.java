package org.robotframework.ide.core.testData.text.context.recognizer.settingTable;

import org.robotframework.ide.core.testData.text.lexer.RobotWordType;


public class TestPreconditionDeclaration extends ASettingTableElementRecognizer {

    public TestPreconditionDeclaration() {
        super(SettingTableRobotContextType.TABLE_SETTINGS_TEST_PRECONDITION,
                createWithAllAsMandatory(RobotWordType.TEST_WORD,
                        RobotWordType.PRECONDITION_WORD));
    }
}
