package org.robotframework.ide.core.testData.text.context.recognizer;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.text.context.ContextBuilder;
import org.robotframework.ide.core.testData.text.context.ContextBuilder.ContextOutput;
import org.robotframework.ide.core.testData.text.context.IContextElement;
import org.robotframework.ide.core.testData.text.context.IContextElementType;
import org.robotframework.ide.core.testData.text.context.OneLineSingleRobotContextPart;
import org.robotframework.ide.core.testData.text.context.SimpleRobotContextType;
import org.robotframework.ide.core.testData.text.context.iterator.TokensLineIterator.LineTokenPosition;
import org.robotframework.ide.core.testData.text.lexer.IRobotTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotSingleCharTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotToken;
import org.robotframework.ide.core.testData.text.lexer.RobotWordType;

import com.google.common.annotations.VisibleForTesting;


/**
 * Check if current line contains empty cells.
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see ContextBuilder
 * @see RobotSingleCharTokenType#SINGLE_ESCAPE_BACKSLASH
 * @see RobotWordType#EMPTY_CELL_DOTS
 * @see RobotSingleCharTokenType#SINGLE_PIPE
 * @see RobotSingleCharTokenType#SINGLE_SPACE
 * @see RobotSingleCharTokenType#SINGLE_TABULATOR
 * @see RobotWordType#DOUBLE_SPACE
 * 
 * @see SimpleRobotContextType#EMPTY_LINE
 */
public class EmptyCellRecognizer implements IContextRecognizer {

    private final static SimpleRobotContextType BUILD_TYPE = SimpleRobotContextType.EMPTY_CELL;


    @Override
    public List<IContextElement> recognize(ContextOutput currentContext,
            LineTokenPosition lineInterval) {
        List<IContextElement> foundContexts = new LinkedList<>();
        OneLineSingleRobotContextPart context = createContext(lineInterval);

        boolean wasDoubleEscapeBackslash = false;
        boolean wasSingleEscapeBackslash = false;
        boolean wasTrash = false;
        List<RobotToken> tokens = currentContext.getTokenizedContent()
                .getTokens();
        for (int tokId = lineInterval.getStart(); tokId < lineInterval.getEnd(); tokId++) {
            RobotToken token = tokens.get(tokId);
            IRobotTokenType type = token.getType();

            if (type == RobotSingleCharTokenType.SINGLE_ESCAPE_BACKSLASH) {
                if (!wasSingleEscapeBackslash && !wasDoubleEscapeBackslash
                        && !wasTrash) {
                    context.addNextToken(token);
                    wasSingleEscapeBackslash = true;
                } else {
                    context.removeAllContextTokens();
                    wasSingleEscapeBackslash = false;
                }
                wasDoubleEscapeBackslash = false;
            } else if (type == RobotSingleCharTokenType.CARRIAGE_RETURN
                    || type == RobotSingleCharTokenType.LINE_FEED) {
                context.addNextToken(token);
            } else if (type == RobotSingleCharTokenType.SINGLE_SPACE
                    || type == RobotSingleCharTokenType.SINGLE_TABULATOR
                    || type == RobotWordType.DOUBLE_SPACE) {
                if (!wasDoubleEscapeBackslash) {
                    if (wasSingleEscapeBackslash) {
                        context.addNextToken(token);
                        context.setType(BUILD_TYPE);
                        foundContexts.add(context);

                        context = createContext(lineInterval);
                    } else {
                        context.removeAllContextTokens();
                        context.addNextToken(token);
                    }
                }
                wasTrash = false;
                wasSingleEscapeBackslash = false;
                wasDoubleEscapeBackslash = false;
            } else if (type == RobotWordType.DOUBLE_ESCAPE_BACKSLASH) {
                // add just for keep track about tokens
                context.removeAllContextTokens();
                wasDoubleEscapeBackslash = true;
                wasSingleEscapeBackslash = false;
            } else if (type == RobotWordType.EMPTY_CELL_DOTS) {
                // doesn't work in robot (..) is treat as normal value
                context.removeAllContextTokens();
                wasDoubleEscapeBackslash = false;
                wasSingleEscapeBackslash = false;
                wasTrash = true;
            } else {
                context.removeAllContextTokens();
                wasDoubleEscapeBackslash = false;
                wasSingleEscapeBackslash = false;
                wasTrash = true;
            }
        }

        if (!wasTrash) {
            boolean isEndUpEmptyCell = false;
            List<RobotToken> contextTokens = context.getContextTokens();
            if (!contextTokens.isEmpty()) {
                for (int i = contextTokens.size() - 1; i > 0; i--) {
                    RobotToken token = contextTokens.get(i);
                    IRobotTokenType type = token.getType();
                    if (type == RobotSingleCharTokenType.CARRIAGE_RETURN
                            || type == RobotSingleCharTokenType.LINE_FEED
                            || type == RobotSingleCharTokenType.SINGLE_SPACE
                            || type == RobotSingleCharTokenType.SINGLE_TABULATOR
                            || type == RobotWordType.DOUBLE_SPACE) {
                        // nothing to do - just skip
                    } else if (type == RobotSingleCharTokenType.SINGLE_ESCAPE_BACKSLASH) {
                        isEndUpEmptyCell = true;
                        break;
                    } else {
                        isEndUpEmptyCell = false;
                        break;
                    }
                }
            }

            if (isEndUpEmptyCell) {
                context.setType(BUILD_TYPE);
                foundContexts.add(context);
            }
        }

        return foundContexts;
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
