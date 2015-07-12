package org.robotframework.ide.core.testData.text.context.recognizer.settingTable;

import org.robotframework.ide.core.testData.text.context.recognizer.ATableElementRecognizer;
import org.robotframework.ide.core.testData.text.lexer.RobotWordType;


/**
 * <pre>
 * *** Settings ***
 * Test Postcondition
 * </pre>
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 */
public class TestPostconditionDeclaration extends ATableElementRecognizer {

    public TestPostconditionDeclaration() {
        super(SettingTableRobotContextType.TABLE_SETTINGS_TEST_POSTCONDITION,
                createExpectedWithOptionalColonAsLast(RobotWordType.TEST_WORD,
                        RobotWordType.POSTCONDITION_WORD));
    }
}
