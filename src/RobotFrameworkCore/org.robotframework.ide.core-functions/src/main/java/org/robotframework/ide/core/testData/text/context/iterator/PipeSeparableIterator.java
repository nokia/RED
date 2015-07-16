package org.robotframework.ide.core.testData.text.context.iterator;

import java.util.Collections;
import java.util.List;

import org.robotframework.ide.core.testData.text.context.AggregatedOneLineRobotContexts;
import org.robotframework.ide.core.testData.text.context.ContextOperationHelper;
import org.robotframework.ide.core.testData.text.context.IContextElement;
import org.robotframework.ide.core.testData.text.context.OneLineSingleRobotContextPart;
import org.robotframework.ide.core.testData.text.context.RobotLineSeparatorsContexts;
import org.robotframework.ide.core.testData.text.context.SimpleRobotContextType;
import org.robotframework.ide.core.testData.text.context.recognizer.ContextElementComparator;
import org.robotframework.ide.core.testData.text.lexer.FilePosition;
import org.robotframework.ide.core.testData.text.lexer.IRobotTokenType;
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
    private int lastFoundSeparatorIndex = 0;
    private final ContextOperationHelper ctxHelper;


    public PipeSeparableIterator(final AggregatedOneLineRobotContexts ctx) {
        this.ctx = ctx;
        this.separators = ctx.getSeparators();
        this.separatorsAndPrettyAlign = separators.getPipeSeparators();
        Collections.sort(separatorsAndPrettyAlign,
                new ContextElementComparator());
        this.separatorsStoreSize = separatorsAndPrettyAlign.size();
        this.ctxHelper = new ContextOperationHelper();
        this.lastTokenColumn = ctxHelper.computeLastTokenColumnPosition(ctx);
    }


    @Override
    public boolean hasNext(FilePosition currentPositionInLine) {
        return (currentPositionInLine.getColumn() < lastTokenColumn);
    }


    @Override
    public RobotSeparatorIteratorOutput next(
            final FilePosition currentPositionInLine) {
        RobotSeparatorIteratorOutput result = null;
        if (hasNext(currentPositionInLine)) {
            result = new RobotSeparatorIteratorOutput(
                    RobotLineSeparatorsContexts.PIPE_SEPARATOR_TYPE);
            int separatorIndex = nextSeparatorIndex(currentPositionInLine);
            if (separatorIndex > -1) {
                fillSeparatorData(result, separatorIndex);
            }
        }

        return result;
    }


    @VisibleForTesting
    protected void fillSeparatorData(final RobotSeparatorIteratorOutput result,
            int separatorIndex) {
        OneLineSingleRobotContextPart separatorContext = (OneLineSingleRobotContextPart) separatorsAndPrettyAlign
                .get(separatorIndex);
        List<RobotToken> contextTokens = separatorContext.getContextTokens();

        StringBuilder leftAlign = new StringBuilder();
        StringBuilder separator = new StringBuilder();
        StringBuilder rightAlign = new StringBuilder();

        RobotToken theFirstToken = contextTokens.get(0);
        IRobotTokenType theFirstTokenType = theFirstToken.getType();
        if (theFirstTokenType == RobotWordType.DOUBLE_SPACE) {
            leftAlign.append(' ');
            separator.append(' ');
        } else {
            separator.append(theFirstToken.getText());
        }

        RobotToken theSecondToken = contextTokens.get(1);
        IRobotTokenType theSecondTokenType = theSecondToken.getType();
        if (theSecondTokenType == RobotWordType.DOUBLE_SPACE) {
            separator.append(' ');
            rightAlign.append(' ');
        } else {
            separator.append(theSecondToken.getText());
        }

        if (contextTokens.size() == 3) {
            RobotToken theThirdToken = contextTokens.get(2);
            IRobotTokenType theThirdTokenType = theThirdToken.getType();
            if (theThirdTokenType == RobotWordType.DOUBLE_SPACE) {
                separator.append(' ');
                rightAlign.append(' ');
            } else {
                separator.append(theThirdToken.getText());
            }
        }

        result.setLeftPrettyAlign(leftAlign);
        result.setSeparator(separator);
        result.setRightPrettyAlign(rightAlign);
    }


    @VisibleForTesting
    protected int nextSeparatorIndex(final FilePosition fp) {
        int result = -1;
        int column = fp.getColumn();
        for (int i = lastFoundSeparatorIndex; i < separatorsStoreSize; i++) {
            IContextElement context = separatorsAndPrettyAlign.get(i);
            if (context.getType() == SimpleRobotContextType.PIPE_SEPARATED) {
                if (context instanceof OneLineSingleRobotContextPart) {
                    OneLineSingleRobotContextPart lineCtx = (OneLineSingleRobotContextPart) context;
                    RobotToken robotToken = lineCtx.getContextTokens().get(0);
                    int tokenStartColumn = robotToken.getStartPosition()
                            .getColumn();
                    if (column == tokenStartColumn) {
                        result = i;
                        lastFoundSeparatorIndex = i;
                        break;
                    }
                } else {
                    ctxHelper.reportProblemWithType(context);
                }
            }
        }

        return result;
    }
}
