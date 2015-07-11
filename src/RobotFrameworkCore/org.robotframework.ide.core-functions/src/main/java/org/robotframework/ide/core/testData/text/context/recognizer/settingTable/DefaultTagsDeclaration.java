package org.robotframework.ide.core.testData.text.context.recognizer.settingTable;

import org.robotframework.ide.core.testData.text.context.recognizer.ATableElementRecognizer;
import org.robotframework.ide.core.testData.text.lexer.RobotWordType;


public class DefaultTagsDeclaration extends ATableElementRecognizer {

    public DefaultTagsDeclaration() {
        super(SettingTableRobotContextType.TABLE_SETTINGS_DEFAULT_TAGS,
                createExpectedWithOptionalColonAsLast(RobotWordType.DEFAULT_WORD,
                        RobotWordType.TAGS_WORD));
    }
}
