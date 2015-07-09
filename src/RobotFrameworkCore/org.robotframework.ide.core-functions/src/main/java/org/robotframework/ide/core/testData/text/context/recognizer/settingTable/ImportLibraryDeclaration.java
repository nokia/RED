package org.robotframework.ide.core.testData.text.context.recognizer.settingTable;

import org.robotframework.ide.core.testData.text.lexer.RobotWordType;


public class ImportLibraryDeclaration extends ASettingTableElementRecognizer {

    public ImportLibraryDeclaration() {
        super(SettingTableRobotContextType.TABLE_SETTINGS_LIBRARY,
                createWithAllAsMandatory(RobotWordType.LIBRARY_WORD));
    }
}
