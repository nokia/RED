package org.robotframework.ide.core.testData.text.context.recognizer.escapeSequences;

import org.robotframework.ide.core.testData.text.context.ContextBuilder;
import org.robotframework.ide.core.testData.text.context.SimpleRobotContextType;
import org.robotframework.ide.core.testData.text.lexer.RobotSingleCharTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotWordType;


/**
 * Check if current line contains literal (word) with new line character
 * {@code \n}
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see ContextBuilder
 * @see RobotSingleCharTokenType#SINGLE_ESCAPE_BACKSLASH
 * @see RobotWordType
 * 
 * @see SimpleRobotContextType#LINE_FEED_TEXT
 */
public class LineFeedTextualRecognizer extends ATextualRecognizer {

    public LineFeedTextualRecognizer() {
        super(SimpleRobotContextType.LINE_FEED_TEXT, 'n', 'N');
    }
}
