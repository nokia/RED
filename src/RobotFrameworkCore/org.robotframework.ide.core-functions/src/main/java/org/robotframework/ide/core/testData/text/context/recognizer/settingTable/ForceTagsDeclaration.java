package org.robotframework.ide.core.testData.text.context.recognizer.settingTable;

import org.robotframework.ide.core.testData.text.context.recognizer.ATableElementRecognizer;
import org.robotframework.ide.core.testData.text.lexer.RobotWordType;


/**
 * <pre>
 * *** Settings ***
 * Force Tags
 * </pre>
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 */
public class ForceTagsDeclaration extends ATableElementRecognizer {

    public ForceTagsDeclaration() {
        super(SettingTableRobotContextType.TABLE_SETTINGS_FORCE_TAGS,
                createExpectedWithOptionalColonAsLast(RobotWordType.FORCE_WORD,
                        RobotWordType.TAGS_WORD));
    }
}
