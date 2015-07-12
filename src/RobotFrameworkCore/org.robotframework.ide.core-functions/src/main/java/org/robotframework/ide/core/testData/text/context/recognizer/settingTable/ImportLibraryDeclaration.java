package org.robotframework.ide.core.testData.text.context.recognizer.settingTable;

import org.robotframework.ide.core.testData.text.context.recognizer.ATableElementRecognizer;
import org.robotframework.ide.core.testData.text.lexer.RobotWordType;


/**
 * <pre>
 * *** Settings ***
 * Library
 * </pre>
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 */
public class ImportLibraryDeclaration extends ATableElementRecognizer {

    public ImportLibraryDeclaration() {
        super(
                SettingTableRobotContextType.TABLE_SETTINGS_LIBRARY,
                createExpectedWithOptionalColonAsLast(RobotWordType.LIBRARY_WORD));
    }
}
