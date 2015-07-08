package org.robotframework.ide.core.testData.text.context.recognizer.escapeSequences;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.text.context.ContextBuilder;
import org.robotframework.ide.core.testData.text.context.ContextBuilder.ContextOutput;
import org.robotframework.ide.core.testData.text.context.IContextElement;
import org.robotframework.ide.core.testData.text.context.IContextElementType;
import org.robotframework.ide.core.testData.text.context.OneLineSingleRobotContextPart;
import org.robotframework.ide.core.testData.text.context.SimpleRobotContextType;
import org.robotframework.ide.core.testData.text.context.TokensLineIterator.LineTokenPosition;
import org.robotframework.ide.core.testData.text.context.recognizer.IContextRecognizer;
import org.robotframework.ide.core.testData.text.lexer.IRobotTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotSingleCharTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotToken;
import org.robotframework.ide.core.testData.text.lexer.RobotWordType;

import com.google.common.annotations.VisibleForTesting;


/**
 * Extracted class for line formatting characters like new line or tabulator.
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see ContextBuilder
 * @see RobotSingleCharTokenType#SINGLE_ESCAPE_BACKSLASH
 * @see RobotWordType
 */
public abstract class ATextualRecognizer implements IContextRecognizer {

    private final SimpleRobotContextType BUILD_TYPE;
    private final char lowerCaseCharRecognized;
    private final char upperCaseCharRecognized;


    protected ATextualRecognizer(final SimpleRobotContextType buildType,
            final char lowerCaseCharRecognized,
            final char upperCaseCharRecognized) {
        this.BUILD_TYPE = buildType;
        this.lowerCaseCharRecognized = lowerCaseCharRecognized;
        this.upperCaseCharRecognized = upperCaseCharRecognized;
    }


    @Override
    public List<IContextElement> recognize(ContextOutput currentContext,
            LineTokenPosition lineInterval) {
        List<IContextElement> foundContexts = new LinkedList<>();
        OneLineSingleRobotContextPart context = createContext(lineInterval);

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
                } else {
                    wasUsed = false;
                }
            } else if (RobotWordType.class.isInstance(type)) {
                if (wasEscape && isStartingFromLetter(token)) {
                    context.addNextToken(token);
                    context.setType(BUILD_TYPE);

                    foundContexts.add(context);
                    context = createContext(lineInterval);
                    wasUsed = true;
                } else {
                    wasUsed = false;
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


    @VisibleForTesting
    protected OneLineSingleRobotContextPart createContext(
            final LineTokenPosition lineInterval) {
        return new OneLineSingleRobotContextPart(lineInterval.getLineNumber());
    }


    @VisibleForTesting
    protected boolean isStartingFromLetter(final RobotToken token) {
        return isStartingFromLetter(extractText(token));
    }


    @VisibleForTesting
    protected boolean isStartingFromLetter(String text) {
        boolean result = false;
        if (text != null && text.length() > 0) {
            char c = text.charAt(0);
            result = (c == lowerCaseCharRecognized || c == upperCaseCharRecognized);
        }
        return result;
    }


    @VisibleForTesting
    protected String extractText(final RobotToken token) {
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
