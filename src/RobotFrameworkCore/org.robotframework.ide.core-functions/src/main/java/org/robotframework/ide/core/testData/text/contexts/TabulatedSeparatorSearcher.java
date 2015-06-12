package org.robotframework.ide.core.testData.text.contexts;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.robotframework.ide.core.testData.text.AContextMatcher;
import org.robotframework.ide.core.testData.text.ContextType;
import org.robotframework.ide.core.testData.text.RobotTokenContext;
import org.robotframework.ide.core.testData.text.RobotTokenType;
import org.robotframework.ide.core.testData.text.TxtRobotFileLexer.TokenizatorOutput;


public class TabulatedSeparatorSearcher extends AContextMatcher {

    public TabulatedSeparatorSearcher(TokenizatorOutput tokenProvider) {
        super(tokenProvider);
    }


    @Override
    protected List<RobotTokenContext> findContexts(
            TokenizatorOutput tokenProvider) throws Exception {
        List<RobotTokenContext> contexts = new LinkedList<>();
        Map<RobotTokenType, List<Integer>> indexesForSeparator = tokenProvider
                .getIndexesForSeparator();

        List<Integer> tabulators = indexesForSeparator
                .get(RobotTokenType.TABULATOR);
        if (tabulators != null) {
            for (Integer tabulatorIndex : tabulators) {
                RobotTokenContext ctx = new RobotTokenContext(
                        ContextType.TABULATOR_SEPARATOR);
                ctx.addToken(tabulatorIndex);
                contexts.add(ctx);
            }
        }

        return contexts;
    }

}
