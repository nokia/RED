package org.robotframework.ide.core.testData.text.context.recognizer.settingTable;

import org.robotframework.ide.core.testData.text.context.recognizer.ATableElementRecognizer;
import org.robotframework.ide.core.testData.text.lexer.RobotWordType;


public class ImportLibraryDeclaration extends ATableElementRecognizer {

    public ImportLibraryDeclaration() {
        super(SettingTableRobotContextType.TABLE_SETTINGS_LIBRARY,
                createExpectedForSettingsTable(RobotWordType.LIBRARY_WORD));
    }
}
