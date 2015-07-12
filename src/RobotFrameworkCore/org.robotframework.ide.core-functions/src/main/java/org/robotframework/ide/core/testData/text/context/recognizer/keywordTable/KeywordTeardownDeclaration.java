package org.robotframework.ide.core.testData.text.context.recognizer.keywordTable;

import org.robotframework.ide.core.testData.text.context.recognizer.ATableElementRecognizer;
import org.robotframework.ide.core.testData.text.lexer.RobotWordType;


/**
 * <pre>
 * *** Keywords ***
 *                  [Teardown] ...
 * </pre>
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 */
public class KeywordTeardownDeclaration extends ATableElementRecognizer {

    public KeywordTeardownDeclaration() {
        super(KeywordTableRobotContextType.TABLE_KEYWORD_SETTINGS_TEARDOWN,
                createExpectedInsideSquareBrackets(RobotWordType.TEARDOWN_WORD));
    }
}
