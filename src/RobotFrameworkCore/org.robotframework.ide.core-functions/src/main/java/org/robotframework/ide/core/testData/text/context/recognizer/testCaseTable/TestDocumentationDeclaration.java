package org.robotframework.ide.core.testData.text.context.recognizer.testCaseTable;

import org.robotframework.ide.core.testData.text.context.recognizer.ATableElementRecognizer;
import org.robotframework.ide.core.testData.text.lexer.RobotWordType;


public class TestDocumentationDeclaration extends ATableElementRecognizer {

    public TestDocumentationDeclaration() {
        super(
                TestCaseTableRobotContextType.TABLE_TEST_CASE_SETTINGS_DOCUMENTATION,
                createExpectedInsideSquareBrackets(RobotWordType.DOCUMENTATION_WORD));
    }
}
