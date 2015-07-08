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
import org.robotframework.ide.core.testData.text.context.recognizer.escapeSequences.AEscapedRecognizer.IRobotWordTypeCustomHandler.CustomHandlerOutput;
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
public abstract class AEscapedRecognizer implements IContextRecognizer {

    private final SimpleRobotContextType BUILD_TYPE;
    private final char lowerCaseCharRecognized;
    private final char upperCaseCharRecognized;
    private IRobotWordTypeCustomHandler customWordTypeHandler;


    protected AEscapedRecognizer(final SimpleRobotContextType buildType,
            final char lowerCaseCharRecognized,
            final char upperCaseCharRecognized) {
        this(buildType, lowerCaseCharRecognized, upperCaseCharRecognized, null);
        this.customWordTypeHandler = this.new DefaultRobotWordTypeCustomHandler();
    }


    protected AEscapedRecognizer(final SimpleRobotContextType buildType,
            final char lowerCaseCharRecognized,
            final char upperCaseCharRecognized,
            final IRobotWordTypeCustomHandler customWordTypeHandler) {
        this.BUILD_TYPE = buildType;
        this.lowerCaseCharRecognized = lowerCaseCharRecognized;
        this.upperCaseCharRecognized = upperCaseCharRecognized;
        this.customWordTypeHandler = customWordTypeHandler;
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
                CustomHandlerOutput customHandlerOutput = getCustomWordTypeHandler()
                        .handle(foundContexts, context, token, wasEscape,
                                lineInterval);
                context = customHandlerOutput.getContextToUseInNextIteration();
                wasEscape = customHandlerOutput.wasEscape();
                wasUsed = customHandlerOutput.wasUsed();
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


    public IRobotWordTypeCustomHandler getCustomWordTypeHandler() {
        return customWordTypeHandler;
    }

    private class DefaultRobotWordTypeCustomHandler implements
            IRobotWordTypeCustomHandler {

        @Override
        public CustomHandlerOutput handle(List<IContextElement> foundContexts,
                OneLineSingleRobotContextPart context, RobotToken token,
                boolean wasEscape, LineTokenPosition lineInterval) {
            CustomHandlerOutput out = new CustomHandlerOutput(foundContexts,
                    context, token, wasEscape);
            if (wasEscape && isStartingFromLetter(token)) {
                context.addNextToken(token);
                context.setType(BUILD_TYPE);

                foundContexts.add(context);
                context = createContext(lineInterval);
                out.setContextToUseInNextIteration(context);
                out.setWasUsed(true);
            } else {
                out.setWasUsed(false);
            }

            return out;
        }
    }

    /**
     * Custom logic related to handling {@link RobotWordType} inside
     * {@link AEscapedRecognizer}.
     * 
     * @author wypych
     * @since JDK 1.7 update 74
     * @version Robot Framework 2.9 alpha 2
     * 
     * @see RobotWordType
     */
    protected interface IRobotWordTypeCustomHandler {

        CustomHandlerOutput handle(final List<IContextElement> foundContexts,
                final OneLineSingleRobotContextPart context,
                final RobotToken token, boolean wasEscape,
                LineTokenPosition lineInterval);

        public static class CustomHandlerOutput {

            private final List<IContextElement> foundContexts;
            private OneLineSingleRobotContextPart contextToUseInNextIteration;
            private RobotToken token;
            private boolean wasEscape = false;
            private boolean wasUsed = false;


            public CustomHandlerOutput(
                    final List<IContextElement> foundContexts,
                    final OneLineSingleRobotContextPart context,
                    final RobotToken token, boolean wasEscape) {
                this.foundContexts = foundContexts;
                this.contextToUseInNextIteration = context;
                this.token = token;
                this.wasEscape = wasEscape;
            }


            public OneLineSingleRobotContextPart getContextToUseInNextIteration() {
                return contextToUseInNextIteration;
            }


            public void setContextToUseInNextIteration(
                    OneLineSingleRobotContextPart contextToUseInNextIteration) {
                this.contextToUseInNextIteration = contextToUseInNextIteration;
            }


            public RobotToken getToken() {
                return token;
            }


            public void setToken(RobotToken token) {
                this.token = token;
            }


            public boolean wasEscape() {
                return wasEscape;
            }


            public void setWasEscape(boolean wasEscape) {
                this.wasEscape = wasEscape;
            }


            public boolean wasUsed() {
                return wasUsed;
            }


            public void setWasUsed(boolean wasUsed) {
                this.wasUsed = wasUsed;
            }


            public List<IContextElement> getFoundContexts() {
                return foundContexts;
            }
        }
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
