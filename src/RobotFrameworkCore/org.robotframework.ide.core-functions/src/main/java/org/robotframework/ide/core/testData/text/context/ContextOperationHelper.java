package org.robotframework.ide.core.testData.text.context;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.text.lexer.FilePosition;
import org.robotframework.ide.core.testData.text.lexer.RobotToken;


public class ContextOperationHelper {

    public List<RobotToken> getWholeLineTokens(
            final AggregatedOneLineRobotContexts ctx) {
        List<RobotToken> tokens = new LinkedList<>();

        List<IContextElement> comments = ctx.getChildContextTypes().get(
                SimpleRobotContextType.UNDECLARED_COMMENT);
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
                List<RobotToken> contextTokens = currentCtx.getContextTokens();
                if (contextTokens != null && !contextTokens.isEmpty()) {
                    RobotToken token = contextTokens.get(0);
                    int distance = token.getStartPosition().getColumn()
                            - column;
                    if (distance > 0) {
                        if (distance < currentDistance) {
                            // we found more near context
                            nearests.clear();
                            currentDistance = distance;
                        }

                        nearests.add(ctx);
                    }
                }
            } else {
                reportProblemWithType(ctx);
            }
        }

        return nearests;
    }
}
