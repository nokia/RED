package org.robotframework.ide.core.testData.text.context.recognizer.settingTable;

import org.robotframework.ide.core.testData.text.context.recognizer.ATableElementRecognizer;
import org.robotframework.ide.core.testData.text.lexer.RobotWordType;


/**
 * <pre>
 * *** Settings ***
 * Resource
 * </pre>
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 */
public class ImportResourceDeclaration extends ATableElementRecognizer {

    public ImportResourceDeclaration() {
        super(
                SettingTableRobotContextType.TABLE_SETTINGS_RESOURCE,
                createExpectedWithOptionalColonAsLast(RobotWordType.RESOURCE_WORD));
    }
}
