package org.robotframework.ide.core.testData.text.context.recognizer.keywordTable;

import org.robotframework.ide.core.testData.text.context.recognizer.ATableElementRecognizer;
import org.robotframework.ide.core.testData.text.lexer.RobotWordType;


public class KeywordReturnDeclaration extends ATableElementRecognizer {

    public KeywordReturnDeclaration() {
        super(KeywordTableRobotContextType.TABLE_KEYWORD_SETTINGS_RETURN,
                createExpectedInsideSquareBrackets(RobotWordType.RETURN_WORD));
    }
}
