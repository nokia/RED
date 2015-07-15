package org.robotframework.ide.core.testData.text.context.iterator;

import java.util.List;

import org.robotframework.ide.core.testData.text.context.AggregatedOneLineRobotContexts;
import org.robotframework.ide.core.testData.text.context.IContextElement;
import org.robotframework.ide.core.testData.text.context.IContextElementType;
import org.robotframework.ide.core.testData.text.context.OneLineSingleRobotContextPart;
import org.robotframework.ide.core.testData.text.context.RobotLineSeparatorsContexts;
import org.robotframework.ide.core.testData.text.lexer.FilePosition;
import org.robotframework.ide.core.testData.text.lexer.IRobotTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotSingleCharTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotToken;

import com.google.common.annotations.VisibleForTesting;


public class SeparatorBaseIteratorBuilder {

    public ContextTokenIterator createSeparatorBaseIterator(
            final AggregatedOneLineRobotContexts ctx) {
        ContextTokenIterator iterator = null;

        RobotLineSeparatorsContexts separators = ctx.getSeparators();
        IContextElement theFirstPipeInLine = getFirstSeparatorContextFrom(
                separators, RobotLineSeparatorsContexts.PIPE_SEPARATOR_TYPE);
        if (isPipeSeparatedLine(theFirstPipeInLine)) {
            iterator = new PipeSeparableIterator(ctx);
        } else {
            iterator = new WhitespaceSeparableIterator(ctx);
        }

        return iterator;
    }


    private boolean isPipeSeparatedLine(IContextElement theFirstPipeInLine) {
        boolean result = false;
        if (theFirstPipeInLine != null) {
            if (theFirstPipeInLine instanceof OneLineSingleRobotContextPart) {
                OneLineSingleRobotContextPart ctx = (OneLineSingleRobotContextPart) theFirstPipeInLine;
                List<RobotToken> contextTokens = ctx.getContextTokens();
                if (contextTokens != null && !contextTokens.isEmpty()) {
                    RobotToken robotToken = contextTokens.get(0);
                    IRobotTokenType type = robotToken.getType();
                    if (type == RobotSingleCharTokenType.SINGLE_PIPE) {
                        result = (FilePosition.THE_FIRST_COLUMN == robotToken
                                .getStartPosition().getColumn());
                    }
                }
            } else {
                throw new IllegalArgumentException(
                        "Pipe separator element has incorrect type "
                                + ((theFirstPipeInLine != null) ? theFirstPipeInLine
                                        .getClass() : " null"));
            }
        }

        return result;
    }


    @VisibleForTesting
    protected IContextElement getFirstSeparatorContextFrom(
            final RobotLineSeparatorsContexts separators,
            final IContextElementType expectedContextType) {
        IContextElement theFirstContext = null;

        List<IContextElement> separatorContext = separators
                .getFoundSeperatorsExcludeType().get(expectedContextType);
        if (separatorContext != null && !separatorContext.isEmpty()) {
            theFirstContext = separatorContext.get(0);
        }

        return theFirstContext;
    }
}
