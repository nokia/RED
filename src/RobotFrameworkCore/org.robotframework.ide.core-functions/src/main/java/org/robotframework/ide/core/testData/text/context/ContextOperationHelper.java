package org.robotframework.ide.core.testData.text.context;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.LineElement;
import org.robotframework.ide.core.testData.model.LineElement.ElementType;
import org.robotframework.ide.core.testData.text.context.iterator.ContextTokenIterator.SeparationType;
import org.robotframework.ide.core.testData.text.lexer.FilePosition;
import org.robotframework.ide.core.testData.text.lexer.RobotToken;

import com.google.common.collect.LinkedListMultimap;


public class ContextOperationHelper {

    public boolean isSeparatorOnly(final List<LineElement> lineElements,
            final SeparationType separatorType) {
        boolean result = true;
        for (LineElement le : lineElements) {
            ElementType theFirstType = le.getElementTypes().get(0);
            if (theFirstType == ElementType.PRETTY_ALIGN) {
                result = true;
            } else if (theFirstType == ElementType.WHITESPACE_SEPARATOR) {
                if (separatorType == SeparationType.WHITESPACES) {
                    result = false;
                    break;
                }
            } else if (theFirstType == ElementType.PIPE_SEPARATOR) {
                result = true;
            } else {
                result = false;
                break;
            }
        }

        return result;
    }


    public int computeLastTokenColumnPosition(
            final AggregatedOneLineRobotContexts ctx) {
        int result = -1;
        List<RobotToken> lineTokens = getWholeLineTokens(ctx);
        if (!lineTokens.isEmpty()) {
            RobotToken lastTokenInLine = lineTokens.get(lineTokens.size() - 1);
            result = lastTokenInLine.getEndPosition().getColumn();
        }

        return result;
    }


    public List<IContextElement> filterByType(
            final List<IContextElement> contexts,
            final IContextElementType expectedType) {
        List<IContextElement> matchingCtxs = new LinkedList<>();
        for (IContextElement ctx : contexts) {
            if (ctx.getType() == expectedType) {
                matchingCtxs.add(ctx);
            }
        }

        return matchingCtxs;
    }


    public List<IContextElement> findContextBetweenPosition(
            final List<IContextElement> contexts, final FilePosition start,
            final FilePosition end) {
        List<IContextElement> matchingCtxs = new LinkedList<>();
        for (IContextElement ctx : contexts) {
            OneLineSingleRobotContextPart c = (OneLineSingleRobotContextPart) ctx;
            List<RobotToken> contextTokens = c.getContextTokens();
            FilePosition thisCtxStart = contextTokens.get(0).getStartPosition();
            FilePosition thisCtxEnd = contextTokens.get(
                    contextTokens.size() - 1).getEndPosition();
            if (thisCtxStart.getLine() == start.getLine()
                    && thisCtxEnd.getLine() == end.getLine()) {
                if (thisCtxStart.getColumn() >= start.getColumn()
                        && thisCtxEnd.getColumn() <= end.getColumn()) {
                    matchingCtxs.add(ctx);
                }
            }
        }

        return matchingCtxs;
    }


    public List<RobotToken> getWholeLineTokens(
            final AggregatedOneLineRobotContexts ctx) {
        List<RobotToken> tokens = new LinkedList<>();

        LinkedListMultimap<IContextElementType, IContextElement> childContextTypes = ctx
                .getChildContextTypes();
        List<IContextElement> comments = childContextTypes
                .get(SimpleRobotContextType.UNDECLARED_COMMENT);
        if (comments.isEmpty()) {
            comments = childContextTypes.get(SimpleRobotContextType.EMPTY_LINE);
        }
        // search for the context with the biggest number of tokens
        for (IContextElement context : comments) {
            if (context instanceof OneLineSingleRobotContextPart) {
                OneLineSingleRobotContextPart currentCtx = (OneLineSingleRobotContextPart) context;
                List<RobotToken> contextTokens = currentCtx.getContextTokens();
                if (contextTokens.size() > tokens.size()) {
                    tokens = contextTokens;
                }
            } else {
                reportProblemWithType(context);
            }
        }

        return tokens;
    }


    public void reportProblemWithType(IContextElement ctx) {
        throw new IllegalArgumentException(String.format(
                "Type %s is not supported.", ((ctx != null) ? ctx.getClass()
                        : "null")));
    }


    public List<IContextElement> filterContextsByColumn(
            final List<IContextElement> childContexts, final FilePosition fp) {
        List<IContextElement> foundCtxs = new LinkedList<>();
        int column = fp.getColumn();

        for (IContextElement ctx : childContexts) {
            if (ctx instanceof OneLineSingleRobotContextPart) {
                OneLineSingleRobotContextPart currentCtx = (OneLineSingleRobotContextPart) ctx;
                List<RobotToken> contextTokens = currentCtx.getContextTokens();
                if (contextTokens != null && !contextTokens.isEmpty()) {
                    RobotToken token = contextTokens.get(0);
                    if (token.getStartPosition().getColumn() >= column) {
                        foundCtxs.add(ctx);
                    }
                }
            } else {
                reportProblemWithType(ctx);
            }
        }

        return foundCtxs;
    }


    public List<IContextElement> findNearestContexts(
            final List<IContextElement> availableContexts, final FilePosition fp) {
        List<IContextElement> nearests = new LinkedList<>();

        int column = fp.getColumn();
        int currentDistance = Integer.MAX_VALUE;
        for (IContextElement ctx : availableContexts) {
            if (ctx instanceof OneLineSingleRobotContextPart) {
                OneLineSingleRobotContextPart currentCtx = (OneLineSingleRobotContextPart) ctx;
                if (currentCtx.getType() != SimpleRobotContextType.UNDECLARED_COMMENT) {
                    List<RobotToken> contextTokens = currentCtx
                            .getContextTokens();
                    if (contextTokens != null && !contextTokens.isEmpty()) {
                        RobotToken token = contextTokens.get(0);
                        int distance = token.getStartPosition().getColumn()
                                - column;
                        if (distance >= 0) {
                            if (distance < currentDistance) {
                                // we found more near context
                                nearests.clear();
                                currentDistance = distance;
                            }

                            if (distance == currentDistance) {
                                nearests.add(ctx);
                            }
                        }
                    }
                }
            } else {
                reportProblemWithType(ctx);
            }
        }

        return nearests;
    }
}
