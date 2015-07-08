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
 * Extracted class for all hex base printable characters.
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see ContextBuilder
 * @see RobotSingleCharTokenType#SINGLE_ESCAPE_BACKSLASH
 * @see RobotWordType
 */
public abstract class ACharacterAsHexValue implements IContextRecognizer {

    private final SimpleRobotContextType BUILD_TYPE;
    private final char startChar;
    private final int numberOfHexChars;


    protected ACharacterAsHexValue(final SimpleRobotContextType buildType,
            char startChar, int numberOfHexChars) {
        this.BUILD_TYPE = buildType;
        this.startChar = startChar;
        this.numberOfHexChars = numberOfHexChars;
    }


    @Override
    public List<IContextElement> recognize(ContextOutput currentContext,
            LineTokenPosition lineInterval) {
        List<IContextElement> foundContexts = new LinkedList<>();
        if (lineInterval != null) {
            OneLineSingleRobotContextPart context = createContext(lineInterval);

            boolean wasEscape = false;
            boolean wasUsed = false;
            List<RobotToken> tokens = currentContext.getTokenizedContent()
                    .getTokens();
            for (int tokId = lineInterval.getStart(); tokId < lineInterval
                    .getEnd(); tokId++) {
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
                    if (wasEscape && isStartingFromLetterAndHexNumber(token)) {
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
        }

        return foundContexts;
    }


    @VisibleForTesting
    protected OneLineSingleRobotContextPart createContext(
            final LineTokenPosition lineInterval) {
        return new OneLineSingleRobotContextPart(lineInterval.getLineNumber());
    }


    @VisibleForTesting
    protected boolean isStartingFromLetterAndHexNumber(final RobotToken token) {
        return isStartingFromLetterAndThenHex(extractText(token));
    }


    @VisibleForTesting
    protected boolean isStartingFromLetterAndThenHex(String text) {
        boolean result = false;
        if (text != null && text.length() >= numberOfHexChars + 1) {
            char c = text.charAt(0);
            result = (c == startChar && isHex(text, numberOfHexChars));
        }
        return result;
    }


    @VisibleForTesting
    protected boolean isHex(String text, int numberOfChars) {
        boolean result = false;
        for (int i = 1; i <= numberOfChars; i++) {
            if (isHex(text.charAt(i))) {
                result = true;
            } else {
                result = false;
                break;
            }
        }

        return result;
    }


    @VisibleForTesting
    protected boolean isHex(char c) {
        return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f')
                || (c >= 'A' && c <= 'F');
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
