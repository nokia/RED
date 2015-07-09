package org.robotframework.ide.core.testData.text.context.recognizer.testCaseTable;

import org.robotframework.ide.core.testData.text.context.recognizer.ATableElementRecognizer;
import org.robotframework.ide.core.testData.text.lexer.RobotWordType;


public class TestPostconditionDeclaration extends ATableElementRecognizer {

    public TestPostconditionDeclaration() {
        super(
                TestCaseTableRobotContextType.TABLE_TEST_CASE_SETTINGS_POSTCONDITION,
                createExpectedInsideSquareBrackets(RobotWordType.POSTCONDITION_WORD));
    }
}
