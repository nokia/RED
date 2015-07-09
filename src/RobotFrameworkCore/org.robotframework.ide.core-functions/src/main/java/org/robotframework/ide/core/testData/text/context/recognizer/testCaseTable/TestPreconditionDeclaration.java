package org.robotframework.ide.core.testData.text.context.recognizer.testCaseTable;

import org.robotframework.ide.core.testData.text.context.recognizer.ATableElementRecognizer;
import org.robotframework.ide.core.testData.text.lexer.RobotWordType;


public class TestPreconditionDeclaration extends ATableElementRecognizer {

    public TestPreconditionDeclaration() {
        super(
                TestCaseTableRobotContextType.TABLE_TEST_CASE_SETTINGS_PRECONDITION,
                createExpectedInsideSquareBrackets(RobotWordType.PRECONDITION_WORD));
    }
}
