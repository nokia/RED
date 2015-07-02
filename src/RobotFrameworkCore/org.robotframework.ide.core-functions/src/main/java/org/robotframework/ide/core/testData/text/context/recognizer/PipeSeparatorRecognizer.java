package org.robotframework.ide.core.testData.text.context.recognizer;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.text.context.ContextBuilder;
import org.robotframework.ide.core.testData.text.context.ContextBuilder.ContextOutput;
import org.robotframework.ide.core.testData.text.context.IContextElement;
import org.robotframework.ide.core.testData.text.context.IContextElementType;
import org.robotframework.ide.core.testData.text.context.OneLineRobotContext;
import org.robotframework.ide.core.testData.text.context.SimpleRobotContextType;
import org.robotframework.ide.core.testData.text.context.TokensLineIterator.LineTokenPosition;
import org.robotframework.ide.core.testData.text.lexer.IRobotTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotSingleCharTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotToken;
import org.robotframework.ide.core.testData.text.lexer.RobotWordType;


/**
 * Search and builds the context for pipe separated line - i.e.
 * 
 * <pre>
 * [whitespace] = [space|double_space|tabulator]
 * | [whitespace] Keyword [whitespace] | [whitespace] ARGUMENT
 * </pre>
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see ContextBuilder
 * @see RobotSingleCharTokenType#SINGLE_TABULATOR
 * @see RobotSingleCharTokenType#SINGLE_PIPE
 * @see RobotSingleCharTokenType#SINGLE_SPACE
 * @see RobotSingleCharTokenType#SINGLE_ESCAPE_BACKSLASH
 * @see RobotWordType#DOUBLE_SPACE
 * 
 * @see SimpleRobotContextType#PIPE_SEPARATED
 * @see SimpleRobotContextType#PRETTY_ALIGN
 * 
 */
public class PipeSeparatorRecognizer implements IContextRecognizer {

    private final static SimpleRobotContextType BUILD_TYPE = SimpleRobotContextType.PIPE_SEPARATED;


    @Override
    public List<IContextElement> recognize(ContextOutput currentContext,
            LineTokenPosition lineInterval) {
        List<IContextElement> foundContexts = new LinkedList<>();
        OneLineRobotContext context = new OneLineRobotContext(
                lineInterval.getLineNumber());

        List<RobotToken> tokens = currentContext.getTokenizedContent()
                .getTokens();

        boolean wasPipe = false;
        boolean wasEscape = false;
        boolean wasPreviousMerged = false;

        for (int tokId = lineInterval.getStart(); tokId < lineInterval.getEnd(); tokId++) {
            RobotToken token = tokens.get(tokId);
            IRobotTokenType type = token.getType();

            if (type == RobotSingleCharTokenType.SINGLE_PIPE) {
                if (wasEscape) {
                    // case \|
                    wasEscape = false;
                } else {
                    if (wasPipe) {
                        // case ||
                        int sizeBefore = foundContexts.size();
                        context = tryToExtractContexts(foundContexts, context,
                                false);
                        int sizeAfter = foundContexts.size();
                        wasPreviousMerged = (sizeBefore != sizeAfter);
                    }

                    context.addNextToken(token);
                    wasPipe = true;
                }
            } else if (type == RobotSingleCharTokenType.SINGLE_TABULATOR
                    || type == RobotWordType.DOUBLE_SPACE
                    || type == RobotSingleCharTokenType.SINGLE_SPACE) {
                context.addNextToken(token);

                if (wasPipe) {
                    int sizeBefore = foundContexts.size();
                    context = tryToExtractContexts(foundContexts, context,
                            wasPreviousMerged);
                    int sizeAfter = foundContexts.size();
                    wasPreviousMerged = (sizeBefore != sizeAfter);
                }

                wasPipe = false;
                wasEscape = false;
            } else if (type == RobotSingleCharTokenType.SINGLE_ESCAPE_BACKSLASH) {
                int sizeBefore = foundContexts.size();
                context = tryToExtractContexts(foundContexts, context, false);
                int sizeAfter = foundContexts.size();
                wasPreviousMerged = (sizeBefore != sizeAfter);

                wasEscape = false;
            } else {
                int sizeBefore = foundContexts.size();
                context = tryToExtractContexts(foundContexts, context,
                        wasPreviousMerged);
                int sizeAfter = foundContexts.size();
                wasPreviousMerged = (sizeBefore != sizeAfter);

                wasPipe = false;
                wasEscape = false;
            }
        }

        tryToExtractContexts(foundContexts, context, true);

        return foundContexts;
    }


    private OneLineRobotContext tryToExtractContexts(
            final List<IContextElement> foundContexts,
            final OneLineRobotContext context, boolean force) {
        OneLineRobotContext newContext = context;

        List<RobotToken> contextTokens = context.getContextTokens();

        if (!contextTokens.isEmpty()) {
            int pipeIndex = findPipeIndex(contextTokens);
            int lineNumber = context.getLineNumber();

            if (force || isNotSingleSpace(contextTokens)) {
                if (pipeIndex == -1) {
                    context.setType(SimpleRobotContextType.PRETTY_ALIGN);
                    foundContexts.add(context);

                    newContext = new OneLineRobotContext(
                            context.getLineNumber());
                } else {
                    OneLineRobotContext beginPrettyAlign = extractPrettyAlignContext(
                            contextTokens, 0, pipeIndex - 2, lineNumber);
                    if (!beginPrettyAlign.getContextTokens().isEmpty()) {
                        foundContexts.add(beginPrettyAlign);
                    }

                    OneLineRobotContext separator = buildPipeSeparatedContext(
                            contextTokens, pipeIndex, lineNumber);
                    foundContexts.add(separator);
                    OneLineRobotContext endPrettyAlign = extractPrettyAlignContext(
                            contextTokens, pipeIndex + 2, contextTokens.size(),
                            lineNumber);
                    if (!endPrettyAlign.getContextTokens().isEmpty()) {
                        foundContexts.add(endPrettyAlign);
                    }
                }
            }

            newContext = new OneLineRobotContext(context.getLineNumber());
        }

        return newContext;
    }


    private boolean isNotSingleSpace(final List<RobotToken> tokens) {
        boolean result = false;

        int tokensSize = tokens.size();
        if (tokensSize == 1) {
            IRobotTokenType type = tokens.get(0).getType();
            result = (type == RobotSingleCharTokenType.SINGLE_TABULATOR || type == RobotWordType.DOUBLE_SPACE);
        } else if (tokensSize > 1) {
            result = true;
        }

        return result;
    }


    private OneLineRobotContext buildPipeSeparatedContext(
            final List<RobotToken> tokens, int pipeIndex, int lineNumber) {
        OneLineRobotContext separator = new OneLineRobotContext(lineNumber);
        if (pipeIndex - 1 >= 0) {
            separator.addNextToken(tokens.get(pipeIndex - 1));
        }
        separator.addNextToken(tokens.get(pipeIndex));

        if (pipeIndex + 1 < tokens.size()) {
            separator.addNextToken(tokens.get(pipeIndex + 1));
        }

        separator.setType(BUILD_TYPE);
        return separator;
    }


    private OneLineRobotContext extractPrettyAlignContext(
            final List<RobotToken> tokens, int from, int to, int lineNumber) {
        OneLineRobotContext pretty = new OneLineRobotContext(lineNumber);

        if (from <= to && from > -1 && to < tokens.size()) {
            for (int i = from; i <= to; i++) {
                pretty.addNextToken(tokens.get(i));
            }
        }

        pretty.setType(SimpleRobotContextType.PRETTY_ALIGN);

        return pretty;
    }


    private int findPipeIndex(final List<RobotToken> tokens) {
        int index = -1;
        int tokensSize = tokens.size();
        for (int i = 0; i < tokensSize; i++) {
            if (tokens.get(i).getType() == RobotSingleCharTokenType.SINGLE_PIPE) {
                index = i;
                break;
            }
        }

        return index;
    }


    @Override
    public IContextElementType getContextType() {
        return BUILD_TYPE;
    }
}
