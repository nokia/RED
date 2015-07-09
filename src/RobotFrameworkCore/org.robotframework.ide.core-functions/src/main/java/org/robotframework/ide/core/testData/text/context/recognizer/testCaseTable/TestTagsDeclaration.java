package org.robotframework.ide.core.testData.text.context.recognizer.testCaseTable;

import org.robotframework.ide.core.testData.text.context.recognizer.ATableElementRecognizer;
import org.robotframework.ide.core.testData.text.lexer.RobotWordType;


public class TestTagsDeclaration extends ATableElementRecognizer {

    public TestTagsDeclaration() {
        super(TestCaseTableRobotContextType.TABLE_TEST_CASE_SETTINGS_TAGS,
                createExpectedInsideSquareBrackets(RobotWordType.TAGS_WORD));
    }
}
