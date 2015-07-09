package org.robotframework.ide.core.testData.text.context.recognizer.settingTable;

import org.robotframework.ide.core.testData.text.context.recognizer.ATableElementRecognizer;
import org.robotframework.ide.core.testData.text.lexer.RobotWordType;


public class DocumentationDeclaration extends ATableElementRecognizer {

    public DocumentationDeclaration() {
        super(
                SettingTableRobotContextType.TABLE_SETTINGS_DOCUMENTATION,
                createExpectedForSettingsTable(RobotWordType.DOCUMENTATION_WORD));
    }
}
