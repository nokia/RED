package org.robotframework.ide.core.testData.text.context.recognizer.settingTable;

import org.robotframework.ide.core.testData.text.context.recognizer.ATableElementRecognizer;
import org.robotframework.ide.core.testData.text.lexer.RobotWordType;


/**
 * <pre>
 * *** Settings ***
 * Suite Postcondition
 * </pre>
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 */
public class SuitePostconditionDeclaration extends ATableElementRecognizer {

    public SuitePostconditionDeclaration() {
        super(SettingTableRobotContextType.TABLE_SETTINGS_SUITE_POSTCONDITION,
                createExpectedWithOptionalColonAsLast(RobotWordType.SUITE_WORD,
                        RobotWordType.POSTCONDITION_WORD));
    }
}
