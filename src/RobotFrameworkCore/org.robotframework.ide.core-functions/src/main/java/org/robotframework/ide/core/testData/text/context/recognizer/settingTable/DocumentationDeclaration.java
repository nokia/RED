package org.robotframework.ide.core.testData.text.context.recognizer.settingTable;

import org.robotframework.ide.core.testData.text.context.recognizer.ATableElementRecognizer;
import org.robotframework.ide.core.testData.text.lexer.RobotWordType;


/**
 * <pre>
 * *** Settings ***
 * Documentation
 * </pre>
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 */
public class DocumentationDeclaration extends ATableElementRecognizer {

    public DocumentationDeclaration() {
        super(
                SettingTableRobotContextType.TABLE_SETTINGS_DOCUMENTATION,
                createExpectedWithOptionalColonAsLast(RobotWordType.DOCUMENTATION_WORD));
    }
}
