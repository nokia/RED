package org.robotframework.ide.core.testData.text.context.recognizer.settingTable;

import org.robotframework.ide.core.testData.text.context.recognizer.ATableElementRecognizer;
import org.robotframework.ide.core.testData.text.lexer.RobotWordType;


/**
 * <pre>
 * *** Settings ***
 * Test Teardown
 * </pre>
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 */
public class TestTeardownDeclaration extends ATableElementRecognizer {

    public TestTeardownDeclaration() {
        super(SettingTableRobotContextType.TABLE_SETTINGS_TEST_TEARDOWN,
                createExpectedWithOptionalColonAsLast(RobotWordType.TEST_WORD,
                        RobotWordType.TEARDOWN_WORD));
    }
}
