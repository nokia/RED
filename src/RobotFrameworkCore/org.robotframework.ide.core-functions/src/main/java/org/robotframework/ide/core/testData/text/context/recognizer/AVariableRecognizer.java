package org.robotframework.ide.core.testData.text.context.recognizer;

import java.util.Collections;
import java.util.Comparator;
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
import org.robotframework.ide.core.testData.text.lexer.FilePosition;
import org.robotframework.ide.core.testData.text.lexer.RobotSingleCharTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotToken;

import com.google.common.annotations.VisibleForTesting;


/**
 * Generic check if current line contains literal variable of expected type with
 * optional position marker
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see ContextBuilder
 * @see RobotSingleCharTokenType#SINGLE_ESCAPE_BACKSLASH
 * @see RobotSingleCharTokenType#SINGLE_VARIABLE_BEGIN_CURLY_BRACKET
 * @see RobotSingleCharTokenType#SINGLE_VARIABLE_END_CURLY_BRACKET
 * @see RobotSingleCharTokenType#SINGLE_POSITION_INDEX_BEGIN_SQUARE_BRACKET
 * @see RobotSingleCharTokenType#SINGLE_POSITION_INDEX_END_SQUARE_BRACKET
 * 
 */
public abstract class AVariableRecognizer implements IContextRecognizer {

    private final IRobotTokenType recognizationTypeInBegin;
    private final SimpleRobotContextType BUILD_TYPE;


    protected AVariableRecognizer(final SimpleRobotContextType buildType,
            final IRobotTokenType recognizationTypeInBegin) {
        this.BUILD_TYPE = buildType;
        this.recognizationTypeInBegin = recognizationTypeInBegin;
    }


    @Override
    public List<IContextElement> recognize(ContextOutput currentContext,
            LineTokenPosition lineInterval) {
        List<IContextElement> foundContexts = new LinkedList<>();
        OneLineSingleRobotContextPart context = createContext(lineInterval);

        List<OneLineSingleRobotContextPart> tempScalars = new LinkedList<>();
        boolean wasEscape = false;

        List<RobotToken> tokens = currentContext.getTokenizedContent()
                .getTokens();
        for (int tokId = lineInterval.getStart(); tokId < lineInterval.getEnd(); tokId++) {
            RobotToken token = tokens.get(tokId);
            IRobotTokenType type = token.getType();

            if (type == recognizationTypeInBegin) {
                // case when we have started already variable and we get escaped
                // variable recognization type
                if (wasEscape) {
                    if (containsBeginRecognizationTypeAndCurrlySigns(context)) {
                        context.addNextToken(token);
                    }

                    wasEscape = false;
                } else {
                    if (wasPreviousTokenOfBeginType(context)
                            && !containsBeginRecognizationTypeAndCurrlySigns(context)) {
                        context.removeAllContextTokens();
                        context.addNextToken(token);
                    } else if (context.getContextTokens().isEmpty()) {
                        context.addNextToken(token);
                    } else {
                        // if its new variable it should be built now until
                        // '} will occurs, the current variable build should
                        // be postpone
                        tempScalars.add(context);
                        context = createContext(lineInterval);
                        context.addNextToken(token);
                    }
                }
            } else if (type == RobotSingleCharTokenType.SINGLE_VARIABLE_BEGIN_CURLY_BRACKET) {
                wasEscape = false;
                if (wasPreviousTokenOfBeginType(context)) {
                    context.addNextToken(token);
                }
            } else {
                int lastElementIndex = tempScalars.size() - 1;
                if (type == RobotSingleCharTokenType.SINGLE_VARIABLE_END_CURLY_BRACKET) {
                    wasEscape = false;
                    if (containsBeginRecognizationTypeAndCurrlySigns(context)) {
                        context.addNextToken(token);
                        context.setType(BUILD_TYPE);
                        foundContexts.add(context);

                        if (!tempScalars.isEmpty()) {
                            context = tempScalars.remove(lastElementIndex);
                        } else {
                            context = createContext(lineInterval);
                        }
                    }
                } else if (type == RobotSingleCharTokenType.SINGLE_ESCAPE_BACKSLASH) {
                    wasEscape = true;
                    if (containsBeginRecognizationTypeAndCurrlySigns(context)) {
                        context.addNextToken(token);
                    }
                } else if (type == RobotSingleCharTokenType.CARRIAGE_RETURN
                        || type == RobotSingleCharTokenType.LINE_FEED) {
                    // nothing to do just to skip and move through them
                } else {
                    wasEscape = false;
                    // case when after recognization type appears not '{', but
                    // different text, we should add current context token to
                    // previous one store or just remove all tokens in case we
                    // do not have any postponed variable
                    if (wasPreviousTokenOfBeginType(context)) {
                        if (!tempScalars.isEmpty()) {
                            OneLineSingleRobotContextPart prev = context;
                            context = tempScalars.remove(lastElementIndex);

                            mergeAndAddInTail(context, prev);
                        } else {
                            context.removeAllContextTokens();
                        }
                    }

                    if (containsBeginRecognizationTypeAndCurrlySigns(context)) {
                        context.addNextToken(token);
                    }
                }
            }
        }

        Collections.sort(foundContexts, new ContextElementComparator());
        return foundContexts;
    }

    @VisibleForTesting
    protected static class ContextElementComparator implements
            Comparator<IContextElement> {

        @Override
        public int compare(IContextElement o1, IContextElement o2) {
            FilePosition lpmO1 = new FilePosition(-1, -1);
            if (o1 instanceof OneLineSingleRobotContextPart) {
                lpmO1 = ((OneLineSingleRobotContextPart) o1).getContextTokens()
                        .get(0).getStartPosition();
            }

            FilePosition lpmO2 = new FilePosition(-1, -1);
            if (o2 instanceof OneLineSingleRobotContextPart) {
                lpmO2 = ((OneLineSingleRobotContextPart) o2).getContextTokens()
                        .get(0).getStartPosition();
            }

            return compareFilePosition(lpmO1, lpmO2);
        }


        @VisibleForTesting
        protected int compareFilePosition(
                final FilePosition o1, final FilePosition o2) {
            int result = 0;

            int o1Line = o1.getLine();
            int o1Column = o1.getColumn();
            int o2Line = o2.getLine();
            int o2Column = o2.getColumn();

            result = Integer.compare(o1Line, o2Line);
            if (result == 0) {
                result = Integer.compare(o1Column, o2Column);
            }

            return result;
        }
    }


    @VisibleForTesting
    protected void mergeAndAddInTail(OneLineSingleRobotContextPart toGetTokens,
            OneLineSingleRobotContextPart toBeMerged) {
        List<RobotToken> contextTokens = toBeMerged.getContextTokens();
        for (RobotToken token : contextTokens) {
            toGetTokens.addNextToken(token);
        }
    }


    @VisibleForTesting
    protected boolean containsBeginRecognizationTypeAndCurrlySigns(
            OneLineSingleRobotContextPart context) {
        boolean result = false;
        List<RobotToken> contextTokens = context.getContextTokens();
        if (contextTokens.size() >= 2) {
            result = contextTokens.get(0).getType() == recognizationTypeInBegin
                    && contextTokens.get(1).getType() == RobotSingleCharTokenType.SINGLE_VARIABLE_BEGIN_CURLY_BRACKET;
        }
        return result;
    }


    @VisibleForTesting
    protected boolean wasPreviousTokenOfBeginType(
            OneLineSingleRobotContextPart context) {
        boolean result = false;
        List<RobotToken> contextTokens = context.getContextTokens();
        if (!contextTokens.isEmpty()) {
            result = contextTokens.get(contextTokens.size() - 1).getType() == recognizationTypeInBegin;
        }

        return result;
    }


    @VisibleForTesting
    protected OneLineSingleRobotContextPart createContext(
            final LineTokenPosition lineInterval) {
        return new OneLineSingleRobotContextPart(lineInterval.getLineNumber());
    }


    @Override
    public IContextElementType getContextType() {
        return BUILD_TYPE;
    }
}
