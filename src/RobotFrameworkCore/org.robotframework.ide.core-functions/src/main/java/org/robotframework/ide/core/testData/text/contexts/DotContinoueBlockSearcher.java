package org.robotframework.ide.core.testData.text.contexts;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.robotframework.ide.core.testData.text.AContextMatcher;
import org.robotframework.ide.core.testData.text.ContextType;
import org.robotframework.ide.core.testData.text.RobotToken;
import org.robotframework.ide.core.testData.text.RobotTokenContext;
import org.robotframework.ide.core.testData.text.RobotTokenType;
import org.robotframework.ide.core.testData.text.TxtRobotFileLexer.TokenizatorOutput;


public class DotContinoueBlockSearcher extends AContextMatcher {

    public DotContinoueBlockSearcher(TokenizatorOutput tokenProvider) {
        super(tokenProvider);
    }


    @Override
    protected List<RobotTokenContext> findContexts(
            TokenizatorOutput tokenProvider) throws Exception {
        List<RobotTokenContext> contexts = new LinkedList<>();

        List<RobotToken> tokens = tokenProvider.getTokens();
        Map<RobotTokenType, List<Integer>> indexesOfSpecial = tokenProvider
                .getIndexesOfSpecial();
        List<Integer> dots = indexesOfSpecial.get(RobotTokenType.DOT);
        if (dots != null) {
            for (Integer dotIndex : dots) {
                if (isContinoue(tokens, dotIndex)) {
                    RobotTokenContext context = new RobotTokenContext(
                            ContextType.DOT_CONTINOUE_PREV_LINE);
                    context.addToken(dotIndex);
                    contexts.add(context);
                }
            }
        }

        return contexts;
    }


    private boolean isContinoue(List<RobotToken> tokens, int dotIndex) {
        boolean result = true;

        for (int checkedIndex = dotIndex - 1; checkedIndex >= 0; checkedIndex--) {
            RobotToken token = tokens.get(checkedIndex);
            RobotTokenType tokenType = token.getType();
            if (tokenType == RobotTokenType.START_LINE) {
                break;
            } else if (tokenType != RobotTokenType.DOT
                    && tokenType != RobotTokenType.SPACE
                    && tokenType != RobotTokenType.TABULATOR
                    && tokenType != RobotTokenType.PIPE) {
                result = false;
                break;
            }
        }

        return result;
    }
}
