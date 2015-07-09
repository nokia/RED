package org.robotframework.ide.core.testData.text.context.recognizer.keywordTable;

import org.robotframework.ide.core.testData.text.context.recognizer.ATableElementRecognizer;
import org.robotframework.ide.core.testData.text.lexer.RobotWordType;


public class KeywordTimeoutDeclaration extends ATableElementRecognizer {

    public KeywordTimeoutDeclaration() {
        super(KeywordTableRobotContextType.TABLE_KEYWORD_SETTINGS_TIMEOUT,
                createExpectedInsideSquareBrackets(RobotWordType.TIMEOUT_WORD));
    }
}
