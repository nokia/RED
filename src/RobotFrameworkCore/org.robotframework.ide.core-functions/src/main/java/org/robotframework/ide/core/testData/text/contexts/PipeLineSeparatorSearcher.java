package org.robotframework.ide.core.testData.text.contexts;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.robotframework.ide.core.testData.text.AContextMatcher;
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

        Map<RobotTokenType, List<Integer>> indexesForSeparator = tokenProvider
                .getIndexesForSeparator();
        List<Integer> pipes = indexesForSeparator.get(RobotTokenType.PIPE);
        List<Integer> spaces = indexesForSeparator.get(RobotTokenType.SPACE);
        List<Integer> tabulators = indexesForSeparator
                .get(RobotTokenType.TABULATOR);
        List<Integer> startLineTokensPosition = tokenProvider
                .getStartLineTokensPosition();

        return contexts;
    }
}
