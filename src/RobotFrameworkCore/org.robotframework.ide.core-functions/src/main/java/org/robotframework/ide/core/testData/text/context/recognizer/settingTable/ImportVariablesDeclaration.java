package org.robotframework.ide.core.testData.text.context.recognizer.settingTable;

import org.robotframework.ide.core.testData.text.context.recognizer.ATableElementRecognizer;
import org.robotframework.ide.core.testData.text.lexer.RobotWordType;


public class ImportVariablesDeclaration extends ATableElementRecognizer {

    public ImportVariablesDeclaration() {
        super(SettingTableRobotContextType.TABLE_SETTINGS_VARIABLES,
                createExpectedWithOptionalColonAsLast(RobotWordType.VARIABLES_WORD));
    }
}
