package org.robotframework.ide.core.testData.text.context.recognizer.settingTable;

import org.robotframework.ide.core.testData.text.context.recognizer.ATableElementRecognizer;
import org.robotframework.ide.core.testData.text.lexer.RobotWordType;


/**
 * <pre>
 * *** Settings ***
 * Library ...  WITH NAME ...
 * </pre>
 * 
 * handle {@code WITH NAME} alias value
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 */
public class ImportLibraryAliasDeclaration extends ATableElementRecognizer {

    public ImportLibraryAliasDeclaration() {
        super(SettingTableRobotContextType.TABLE_SETTINGS_LIBRARY_ALIASES,
                createExpectedAllMandatory(RobotWordType.WITH_WORD,
                        RobotWordType.NAME_WORD));
    }
}
