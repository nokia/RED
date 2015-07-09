package org.robotframework.ide.core.testData.text.context.recognizer.settingTable;

import org.robotframework.ide.core.testData.text.lexer.RobotWordType;


public class ImportResourceDeclaration extends ASettingTableElementRecognizer {

    public ImportResourceDeclaration() {
        super(SettingTableRobotContextType.TABLE_SETTINGS_RESOURCE,
                createWithAllAsMandatory(RobotWordType.RESOURCE_WORD));
    }
}
