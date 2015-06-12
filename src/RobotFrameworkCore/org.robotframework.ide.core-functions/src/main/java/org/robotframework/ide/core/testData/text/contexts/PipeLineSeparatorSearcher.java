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


public class PipeLineSeparatorSearcher extends AContextMatcher {

    public PipeLineSeparatorSearcher(TokenizatorOutput tokenProvider) {
        super(tokenProvider);
    }


    @Override
    protected List<RobotTokenContext> findContexts(
            TokenizatorOutput tokenProvider) throws Exception {
        List<RobotTokenContext> contexts = new LinkedList<>();
        List<RobotToken> tokens = tokenProvider.getTokens();
        Map<RobotTokenType, List<Integer>> indexesForSeparator = tokenProvider
                .getIndexesForSeparator();
        List<Integer> indexOfPipes = indexesForSeparator
                .get(RobotTokenType.PIPE);
        if (indexOfPipes != null) {
            for (Integer pipeIndex : indexOfPipes) {
                RobotToken previousToken = tokens.get(pipeIndex - 1);
                RobotTokenType previousTokenType = previousToken.getType();
                RobotToken nextToken = tokens.get(pipeIndex + 1);
                RobotTokenType nextTokenType = nextToken.getType();

                boolean isBeginCorrect = false;
                if (previousTokenType == RobotTokenType.START_LINE
                        || previousTokenType == RobotTokenType.SPACE
                        || previousTokenType == RobotTokenType.TABULATOR) {
                    isBeginCorrect = true;
                }
                boolean isEndCorrect = false;
                if (nextTokenType == RobotTokenType.END_OF_FILE
                        || nextTokenType == RobotTokenType.END_OF_LINE
                        || nextTokenType == RobotTokenType.SPACE
                        || nextTokenType == RobotTokenType.TABULATOR) {
                    isEndCorrect = true;
                }

                if (isBeginCorrect || isEndCorrect) {
                    RobotTokenContext nextContext = new RobotTokenContext(
                            ContextType.PIPE_SEPARATOR);
                    if (isBeginCorrect) {
                        nextContext.addToken(pipeIndex - 1);
                    }

                    nextContext.addToken(pipeIndex);

                    if (isEndCorrect) {
                        nextContext.addToken(pipeIndex + 1);
                    }

                    contexts.add(nextContext);
                }
            }
        }

        return contexts;
    }
}
