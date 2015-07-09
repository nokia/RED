package org.robotframework.ide.core.testData.text.context.recognizer.testCaseTable;

import org.robotframework.ide.core.testData.text.context.recognizer.ATableElementRecognizer;
import org.robotframework.ide.core.testData.text.lexer.RobotWordType;


public class TestTimeoutDeclaration extends ATableElementRecognizer {

    public TestTimeoutDeclaration() {
        super(TestCaseTableRobotContextType.TABLE_TEST_CASE_SETTINGS_TIMEOUT,
                createExpectedInsideSquareBrackets(RobotWordType.TIMEOUT_WORD));
    }
}
