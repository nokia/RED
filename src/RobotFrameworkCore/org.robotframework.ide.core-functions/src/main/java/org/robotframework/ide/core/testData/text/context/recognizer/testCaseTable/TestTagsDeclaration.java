package org.robotframework.ide.core.testData.text.context.recognizer.testCaseTable;

import org.robotframework.ide.core.testData.text.context.recognizer.ATableElementRecognizer;
import org.robotframework.ide.core.testData.text.lexer.RobotWordType;


/**
 * <pre>
 * *** Test Case ***
 *                  [Tags]
 * </pre>
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 */
public class TestTagsDeclaration extends ATableElementRecognizer {

    public TestTagsDeclaration() {
        super(TestCaseTableRobotContextType.TABLE_TEST_CASE_SETTINGS_TAGS,
                createExpectedInsideSquareBrackets(RobotWordType.TAGS_WORD));
    }
}
