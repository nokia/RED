package org.robotframework.ide.core.testData.text.context.recognizer.settingTable;

import org.robotframework.ide.core.testData.text.lexer.RobotWordType;


public class DocumentationDeclaration extends ASettingTableElementRecognizer {

    public DocumentationDeclaration() {
        super(SettingTableRobotContextType.TABLE_SETTINGS_DOCUMENTATION,
                createWithAllAsMandatory(RobotWordType.DOCUMENTATION_WORD));
    }
}
