package org.robotframework.ide.core.testData.text.context.recognizer.testCaseTable;

import org.robotframework.ide.core.testData.text.context.recognizer.ATableElementRecognizer;
import org.robotframework.ide.core.testData.text.lexer.RobotWordType;


public class TestTeardownDeclaration extends ATableElementRecognizer {

    public TestTeardownDeclaration() {
        super(TestCaseTableRobotContextType.TABLE_TEST_CASE_SETTINGS_TEARDOWN,
                createExpectedInsideSquareBrackets(RobotWordType.TEARDOWN_WORD));
    }
}
