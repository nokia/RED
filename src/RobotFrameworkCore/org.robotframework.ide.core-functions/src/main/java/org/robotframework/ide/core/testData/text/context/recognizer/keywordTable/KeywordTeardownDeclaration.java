package org.robotframework.ide.core.testData.text.context.recognizer.keywordTable;

import org.robotframework.ide.core.testData.text.context.recognizer.ATableElementRecognizer;
import org.robotframework.ide.core.testData.text.lexer.RobotWordType;


public class KeywordTeardownDeclaration extends ATableElementRecognizer {

    public KeywordTeardownDeclaration() {
        super(KeywordTableRobotContextType.TABLE_KEYWORD_SETTINGS_TEARDOWN,
                createExpectedInsideSquareBrackets(RobotWordType.TEARDOWN_WORD));
    }
}
