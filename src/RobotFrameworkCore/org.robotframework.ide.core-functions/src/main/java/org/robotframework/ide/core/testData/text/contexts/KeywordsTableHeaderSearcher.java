package org.robotframework.ide.core.testData.text.contexts;

import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.robotframework.ide.core.testData.text.ATableContextMatcher;
import org.robotframework.ide.core.testData.text.ContextType;
import org.robotframework.ide.core.testData.text.RobotTokenContext;
import org.robotframework.ide.core.testData.text.RobotTokenType;
import org.robotframework.ide.core.testData.text.TxtRobotFileLexer.TokenizatorOutput;


public class KeywordsTableHeaderSearcher extends ATableContextMatcher {

    public KeywordsTableHeaderSearcher(TokenizatorOutput tokenProvider) {
        super(tokenProvider);
    }


    @Override
    protected List<RobotTokenContext> findContexts(
            TokenizatorOutput tokenProvider)
            throws ConcurrentModificationException, InterruptedException,
            ExecutionException {
        ContextType type = ContextType.KEYWORDS_TABLE_HEADER;
        List<List<RobotTokenType>> combinationsExpected = new LinkedList<>();
        combinationsExpected.add(Arrays
                .asList(new RobotTokenType[] { RobotTokenType.WORD_KEYWORD }));
        combinationsExpected.add(Arrays.asList(new RobotTokenType[] {
                RobotTokenType.WORD_USER, RobotTokenType.WORD_KEYWORD }));
        return buildTableHeaderContext(tokenProvider, type,
                combinationsExpected);
    }
}
