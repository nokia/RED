package org.robotframework.ide.core.testData.text.context.recognizer.settingTable;

import org.robotframework.ide.core.testData.text.lexer.RobotWordType;


public class DefaultTagsDeclaration extends ASettingTableElementRecognizer {

    public DefaultTagsDeclaration() {
        super(SettingTableRobotContextType.TABLE_SETTINGS_DEFAULT_TAGS,
                createWithAllAsMandatory(RobotWordType.DEFAULT_WORD,
                        RobotWordType.TAGS_WORD));
    }
}
