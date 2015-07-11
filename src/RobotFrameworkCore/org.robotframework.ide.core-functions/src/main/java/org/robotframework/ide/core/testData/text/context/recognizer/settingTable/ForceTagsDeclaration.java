package org.robotframework.ide.core.testData.text.context.recognizer.settingTable;

import org.robotframework.ide.core.testData.text.context.recognizer.ATableElementRecognizer;
import org.robotframework.ide.core.testData.text.lexer.RobotWordType;


public class ForceTagsDeclaration extends ATableElementRecognizer {

    public ForceTagsDeclaration() {
        super(SettingTableRobotContextType.TABLE_SETTINGS_FORCE_TAGS,
                createExpectedWithOptionalColonAsLast(RobotWordType.FORCE_WORD,
                        RobotWordType.TAGS_WORD));
    }
}
