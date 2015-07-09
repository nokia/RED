package org.robotframework.ide.core.testData.text.context.recognizer.keywordTable;

import org.robotframework.ide.core.testData.text.context.recognizer.ATableElementRecognizer;
import org.robotframework.ide.core.testData.text.lexer.RobotWordType;


public class KeywordDocumentationDeclaration extends ATableElementRecognizer {

    public KeywordDocumentationDeclaration() {
        super(
                KeywordTableRobotContextType.TABLE_KEYWORD_SETTINGS_DOCUMENTATION,
                createExpectedInsideSquareBrackets(RobotWordType.DOCUMENTATION_WORD));
    }
}
