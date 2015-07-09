package org.robotframework.ide.core.testData.text.context.recognizer.settingTable;

import org.robotframework.ide.core.testData.text.lexer.RobotWordType;


public class ImportVariablesDeclaration extends ASettingTableElementRecognizer {

    public ImportVariablesDeclaration() {
        super(SettingTableRobotContextType.TABLE_SETTINGS_VARIABLES,
                createWithAllAsMandatory(RobotWordType.VARIABLES_WORD));
    }
}
