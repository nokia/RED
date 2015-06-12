package org.robotframework.ide.core.testData.text.contexts;

import java.util.List;

import org.robotframework.ide.core.testData.text.AContextMatcher;
import org.robotframework.ide.core.testData.text.RobotTokenContext;
import org.robotframework.ide.core.testData.text.TxtRobotFileLexer.TokenizatorOutput;


public class EscapedCharsBlockSearcher extends AContextMatcher {

    public EscapedCharsBlockSearcher(TokenizatorOutput tokenProvider) {
        super(tokenProvider);
    }


    @Override
    protected List<RobotTokenContext> findContexts(
            TokenizatorOutput tokenProvider) throws Exception {

        return null;
    }

}
