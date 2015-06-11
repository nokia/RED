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


public class TestCaseTableHeaderSearcher extends ATableContextMatcher {

    public TestCaseTableHeaderSearcher(TokenizatorOutput tokenProvider) {
        super(tokenProvider);
    }


    @Override
    protected List<RobotTokenContext> findContexts(
            TokenizatorOutput tokenProvider)
            throws ConcurrentModificationException, InterruptedException,
            ExecutionException {
        ContextType type = ContextType.TEST_CASES_TABLE_HEADER;
        List<List<RobotTokenType>> combinationsExpected = new LinkedList<>();
        combinationsExpected.add(Arrays.asList(new RobotTokenType[] {
                RobotTokenType.WORD_TEST, RobotTokenType.WORD_CASE }));
        return buildTableHeaderContext(tokenProvider, type,
                combinationsExpected);
    }
}
