package org.robotframework.ide.core.testData.text.context.recognizer.testCaseTable;

import org.robotframework.ide.core.testData.text.context.recognizer.ATableElementRecognizer;
import org.robotframework.ide.core.testData.text.lexer.RobotWordType;


public class TestSetupDeclaration extends ATableElementRecognizer {

    public TestSetupDeclaration() {
        super(TestCaseTableRobotContextType.TABLE_TEST_CASE_SETTINGS_SETUP,
                createExpectedInsideSquareBrackets(RobotWordType.SETUP_WORD));
    }
}
