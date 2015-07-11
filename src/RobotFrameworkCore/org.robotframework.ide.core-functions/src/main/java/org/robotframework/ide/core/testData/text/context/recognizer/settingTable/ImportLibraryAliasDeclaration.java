package org.robotframework.ide.core.testData.text.context.recognizer.settingTable;

import org.robotframework.ide.core.testData.text.context.recognizer.ATableElementRecognizer;
import org.robotframework.ide.core.testData.text.lexer.RobotWordType;


public class ImportLibraryAliasDeclaration extends ATableElementRecognizer {

    public ImportLibraryAliasDeclaration() {
        super(SettingTableRobotContextType.TABLE_SETTINGS_LIBRARY_ALIASES,
                createExpectedWithOptionalColonAsLast(RobotWordType.WITH_WORD,
                        RobotWordType.NAME_WORD));
    }
}
