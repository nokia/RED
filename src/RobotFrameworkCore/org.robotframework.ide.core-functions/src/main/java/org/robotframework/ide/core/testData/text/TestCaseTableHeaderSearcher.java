package org.robotframework.ide.core.testData.text;

import java.util.List;

import org.robotframework.ide.core.testData.text.TxtRobotFileLexer.TokenizatorOutput;


public class TestCaseTableHeaderSearcher extends AContextMatcher {

    public TestCaseTableHeaderSearcher(TokenizatorOutput tokenProvider) {
        super(tokenProvider);
    }


    @Override
    protected List<RobotTokenContext> findContexts(
            TokenizatorOutput tokenProvider) {

        ContextType type = ContextType.TEST_CASES_TABLE_HEADER;

        return null;
    }
}
