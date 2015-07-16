package org.robotframework.ide.core.testData.text.context.iterator;

import java.util.Collections;
import java.util.List;

import org.robotframework.ide.core.testData.text.context.AggregatedOneLineRobotContexts;
import org.robotframework.ide.core.testData.text.context.IContextElement;
import org.robotframework.ide.core.testData.text.context.OneLineSingleRobotContextPart;
import org.robotframework.ide.core.testData.text.context.RobotLineSeparatorsContexts;
import org.robotframework.ide.core.testData.text.context.recognizer.ContextElementComparator;
import org.robotframework.ide.core.testData.text.lexer.FilePosition;
import org.robotframework.ide.core.testData.text.lexer.IRobotTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotSingleCharTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotToken;
import org.robotframework.ide.core.testData.text.lexer.RobotWordType;

import com.google.common.annotations.VisibleForTesting;


public class PipeSeparableIterator implements ContextTokenIterator {

    @SuppressWarnings("unused")
    private final AggregatedOneLineRobotContexts ctx;
    private final RobotLineSeparatorsContexts separators;
    private final List<IContextElement> separatorsAndPrettyAlign;
    private final int separatorsStoreSize;
    private final int lastTokenColumn;


    public PipeSeparableIterator(final AggregatedOneLineRobotContexts ctx) {
        this.ctx = ctx;
        this.separators = ctx.getSeparators();
        this.separatorsAndPrettyAlign = separators.getPipeSeparators();
        Collections.sort(separatorsAndPrettyAlign,
                new ContextElementComparator());
        this.separatorsStoreSize = separatorsAndPrettyAlign.size();
        this.lastTokenColumn = computeLastTokenColumnPosition(ctx);
    }


    @VisibleForTesting
    protected int computeLastTokenColumnPosition(
            final AggregatedOneLineRobotContexts ctx) {
        int result = -1;
        List<IContextElement> childContexts = ctx.getChildContexts();
        if (childContexts != null && !childContexts.isEmpty()) {
            IContextElement lastCtx = childContexts
                    .get(childContexts.size() - 1);
            if (lastCtx instanceof OneLineSingleRobotContextPart) {
                OneLineSingleRobotContextPart last = (OneLineSingleRobotContextPart) lastCtx;
                List<RobotToken> contextTokens = last.getContextTokens();
                RobotToken lastToken = contextTokens
                        .get(contextTokens.size() - 1);
                result = lastToken.getEndPosition().getColumn();
            } else {
                reportProblemWithType(lastCtx);
            }
        }

        return result;
    }


    @VisibleForTesting
    protected void reportProblemWithType(IContextElement lastCtx) {
        throw new IllegalArgumentException(String.format(
                "Type %s is not supported.",
                ((lastCtx != null) ? lastCtx.getClass() : "null")));
    }


    @VisibleForTesting
    protected int nextSeparator(final FilePosition currentPositionInLine) {
        int result = -1;

        int expectedColumn = currentPositionInLine.getColumn();
        for (int i = 0; i < separatorsStoreSize; i++) {
            IContextElement elem = separatorsAndPrettyAlign.get(i);
            if (elem instanceof OneLineSingleRobotContextPart) {
                OneLineSingleRobotContextPart ctx = (OneLineSingleRobotContextPart) elem;
                if (ctx.getType() == RobotLineSeparatorsContexts.PIPE_SEPARATOR_TYPE) {
                    if (!ctx.getContextTokens().isEmpty()) {
                        int column = ctx.getContextTokens().get(0)
                                .getStartPosition().getColumn();
                        if (column == expectedColumn) {
                            result = i;
                            break;
                        }
                    }
                }
            } else {
                reportProblemWithType(elem);
            }
        }

        return result;
    }


    @Override
    public boolean hasNext(FilePosition currentPositionInLine) {
        return (currentPositionInLine.getColumn() < lastTokenColumn);
    }


    @Override
    public RobotSeparatorIteratorOutput next(
            final FilePosition currentPositionInLine) {
        RobotSeparatorIteratorOutput result;

        int nextSeparator = nextSeparator(currentPositionInLine);
        if (hasNext(currentPositionInLine)) {
            result = new RobotSeparatorIteratorOutput(
                    RobotLineSeparatorsContexts.PIPE_SEPARATOR_TYPE);
            if (nextSeparator > -1) {
                fill(result, nextSeparator);
            }
        } else {
            // null means no more token to map in this line
            result = null;
        }

        return result;
    }


    private void fill(RobotSeparatorIteratorOutput result, int nextSeparator) {
        IContextElement currentSeparator = separatorsAndPrettyAlign
                .get(nextSeparator);
        if (currentSeparator instanceof OneLineSingleRobotContextPart) {
            OneLineSingleRobotContextPart separatorPlusAlign = (OneLineSingleRobotContextPart) currentSeparator;
            List<RobotToken> contextTokens = separatorPlusAlign
                    .getContextTokens();

            final StringBuilder leftPrettyAlign = new StringBuilder();
            final StringBuilder separator = new StringBuilder();
            final StringBuilder rightPrettyAlign = new StringBuilder();

            RobotToken theFirst = contextTokens.get(0);
            IRobotTokenType theFirstTokenType = theFirst.getType();
            if (theFirstTokenType == RobotSingleCharTokenType.SINGLE_PIPE) {
                if (theFirst.getStartPosition().getColumn() == FilePosition.THE_FIRST_COLUMN) {
                    // check if is not case: ||${SPACE}
                    separator.append(theFirst.getText());
                    // it means we have case | ${WHITESPACE}
                    RobotToken theSecond = contextTokens.get(1);
                    IRobotTokenType theSecondTokenType = theSecond.getType();

                    if (theSecondTokenType == RobotWordType.DOUBLE_SPACE) {
                        separator.append(' ');
                        rightPrettyAlign.append(' ');
                    } else {
                        separator.append(theSecond.getText());
                    }
                }
            } else if (theFirstTokenType == RobotWordType.DOUBLE_SPACE) {
                leftPrettyAlign.append(' ');
                separator.append(" |");
                // case ${WHITESPACE} |
            } else {
                // space case
                separator.append(' ');
            }

            if (contextTokens.size() == 3) {
                RobotToken theThird = contextTokens.get(2);
                IRobotTokenType theThirdType = theThird.getType();

                if (theThirdType == RobotWordType.DOUBLE_SPACE) {
                    separator.append(' ');
                    rightPrettyAlign.append(' ');
                } else {
                    rightPrettyAlign.append(theThird.getText());
                }
            }

            result.setLeftPrettyAlign(leftPrettyAlign);
            result.setSeparator(separator);
            result.setRightPrettyAlign(rightPrettyAlign);
        } else {
            reportProblemWithType(currentSeparator);
        }
    }
}
