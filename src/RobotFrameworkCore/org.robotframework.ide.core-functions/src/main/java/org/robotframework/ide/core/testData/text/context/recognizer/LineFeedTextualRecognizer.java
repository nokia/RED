package org.robotframework.ide.core.testData.text.context.recognizer;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.text.context.ContextBuilder;
import org.robotframework.ide.core.testData.text.context.ContextBuilder.ContextOutput;
import org.robotframework.ide.core.testData.text.context.IContextElement;
import org.robotframework.ide.core.testData.text.context.IContextElementType;
import org.robotframework.ide.core.testData.text.context.OneLineSingleRobotContextPart;
import org.robotframework.ide.core.testData.text.context.SimpleRobotContextType;
import org.robotframework.ide.core.testData.text.context.TokensLineIterator.LineTokenPosition;
import org.robotframework.ide.core.testData.text.lexer.IRobotTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotSingleCharTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotToken;
import org.robotframework.ide.core.testData.text.lexer.RobotWordType;


/**
 * Check if current line contains literal (word) comment or comment by hash
 * sign.
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
public class LineFeedTextualRecognizer implements IContextRecognizer {

    private final static SimpleRobotContextType BUILD_TYPE = SimpleRobotContextType.LINE_FEED_TEXT;


    @Override
    public List<IContextElement> recognize(ContextOutput currentContext,
            LineTokenPosition lineInterval) {
        List<IContextElement> foundContexts = new LinkedList<>();
        OneLineSingleRobotContextPart context = new OneLineSingleRobotContextPart(
                lineInterval.getLineNumber());

        boolean wasEscape = false;
        boolean wasUsed = false;
        List<RobotToken> tokens = currentContext.getTokenizedContent()
                .getTokens();
        for (int tokId = lineInterval.getStart(); tokId < lineInterval.getEnd(); tokId++) {
            RobotToken token = tokens.get(tokId);
            IRobotTokenType type = token.getType();

            if (type == RobotSingleCharTokenType.SINGLE_ESCAPE_BACKSLASH) {
                if (!wasEscape) {
                    context.addNextToken(token);
                    wasEscape = true;
                    wasUsed = true;
                }
            } else if (RobotWordType.class.isInstance(type)) {
                if (wasEscape && isStartingFromN(token)) {
                    context.addNextToken(token);
                    context.setType(BUILD_TYPE);

                    foundContexts.add(context);
                    context = new OneLineSingleRobotContextPart(
                            lineInterval.getLineNumber());
                    wasUsed = true;
                }
            } else {
                wasUsed = false;
            }

            if (!wasUsed) {
                wasEscape = false;
                context.removeAllContextTokens();
            }
        }

        return foundContexts;
    }


    private boolean isStartingFromN(final RobotToken token) {
        return isStartingFromN(extractText(token));
    }


    private boolean isStartingFromN(String text) {
        boolean result = false;
        if (text != null && text.length() > 0) {
            char c = text.charAt(0);
            result = (c == 'n' || c == 'N');
        }
        return result;
    }


    private String extractText(final RobotToken token) {
        String value = null;
        StringBuilder text = token.getText();
        if (text != null) {
            value = text.toString();
        }

        return value;
    }


    @Override
    public IContextElementType getContextType() {
        return BUILD_TYPE;
    }
}
