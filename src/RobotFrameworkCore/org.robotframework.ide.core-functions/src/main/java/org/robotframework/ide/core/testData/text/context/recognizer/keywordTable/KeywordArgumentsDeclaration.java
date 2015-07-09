package org.robotframework.ide.core.testData.text.context.recognizer.keywordTable;

import org.robotframework.ide.core.testData.text.context.recognizer.ATableElementRecognizer;
import org.robotframework.ide.core.testData.text.lexer.RobotWordType;


public class KeywordArgumentsDeclaration extends ATableElementRecognizer {

    public KeywordArgumentsDeclaration() {
        super(
                KeywordTableRobotContextType.TABLE_KEYWORD_SETTINGS_ARGUMENTS,
                createExpectedInsideSquareBrackets(RobotWordType.ARGUMENTS_WORD));
    }
}
