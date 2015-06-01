package org.robotframework.ide.core.testData.text.contexts;

import java.util.List;

import org.robotframework.ide.core.testData.text.AContextMatcher;
import org.robotframework.ide.core.testData.text.ContextType;
import org.robotframework.ide.core.testData.text.RobotTokenContext;
import org.robotframework.ide.core.testData.text.TxtRobotFileLexer;
import org.robotframework.ide.core.testData.text.TxtRobotFileLexer.TokenizatorOutput;


public class KeywordsTableHeaderSearcher extends AContextMatcher {

    public KeywordsTableHeaderSearcher(TokenizatorOutput tokenProvider) {
        super(tokenProvider);
    }


    @Override
    protected List<RobotTokenContext> findContexts(
            TokenizatorOutput tokenProvider) {
        ContextType type = ContextType.KEYWORDS_TABLE_HEADER;

        return null;
    }
}
