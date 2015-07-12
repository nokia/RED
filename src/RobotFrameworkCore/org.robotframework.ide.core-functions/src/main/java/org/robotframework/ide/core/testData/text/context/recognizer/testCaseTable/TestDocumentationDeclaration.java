package org.robotframework.ide.core.testData.text.context.recognizer.testCaseTable;

import org.robotframework.ide.core.testData.text.context.recognizer.ATableElementRecognizer;
import org.robotframework.ide.core.testData.text.lexer.RobotWordType;


/**
 * <pre>
 * *** Test Case ***
 *                  [Documentation]
 * </pre>
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 */
public class TestDocumentationDeclaration extends ATableElementRecognizer {

    public TestDocumentationDeclaration() {
        super(
                TestCaseTableRobotContextType.TABLE_TEST_CASE_SETTINGS_DOCUMENTATION,
                createExpectedInsideSquareBrackets(RobotWordType.DOCUMENTATION_WORD));
    }
}
