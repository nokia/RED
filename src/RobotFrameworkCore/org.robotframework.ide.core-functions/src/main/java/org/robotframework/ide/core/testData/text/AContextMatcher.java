package org.robotframework.ide.core.testData.text;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

import org.robotframework.ide.core.testData.text.TxtRobotFileLexer.TokenizatorOutput;


public abstract class AContextMatcher implements Callable<List<Context>> {

    protected final TokenizatorOutput tokenProvider;
    private int lineNumber;


    public AContextMatcher(final TokenizatorOutput tokenProvider) {
        this.tokenProvider = tokenProvider;
        this.lineNumber = -1;
    }


    public abstract List<Context> findContexts(int lineNumber);


    public void setLineToMatch(int lineNumber) {
        this.lineNumber = lineNumber;
    }


    @Override
    public List<Context> call() throws Exception {
        List<Context> placesOfContext = findContexts(lineNumber);

        if (placesOfContext == null) {
            placesOfContext = new LinkedList<>();
        }

        return placesOfContext;
    }
}
