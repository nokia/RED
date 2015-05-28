package org.robotframework.ide.core.testData.text;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

import org.robotframework.ide.core.testData.text.TxtRobotFileLexer.TokenizatorOutput;


public abstract class AContextMatcher implements
        Callable<List<RobotTokenContext>> {

    protected final TokenizatorOutput tokenProvider;


    public AContextMatcher(final TokenizatorOutput tokenProvider) {
        this.tokenProvider = tokenProvider;
    }


    protected abstract List<RobotTokenContext> findContexts(
            final TokenizatorOutput tokenProvider);


    @Override
    public List<RobotTokenContext> call() throws Exception {
        List<RobotTokenContext> placesOfContext = findContexts(tokenProvider);

        if (placesOfContext == null) {
            placesOfContext = new LinkedList<>();
        }

        return placesOfContext;
    }
}
