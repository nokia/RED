package org.robotframework.ide.core.testData.text.context.recognizer.testCaseTable;

import org.robotframework.ide.core.testData.text.context.recognizer.ATableElementRecognizer;
import org.robotframework.ide.core.testData.text.lexer.RobotWordType;


public class TestTemplateDeclaration extends ATableElementRecognizer {

    public TestTemplateDeclaration() {
        super(TestCaseTableRobotContextType.TABLE_TEST_CASE_SETTINGS_TEMPLATE,
                createExpectedInsideSquareBrackets(RobotWordType.TEMPLATE_WORD));
    }
}
