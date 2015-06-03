package org.robotframework.ide.core.testData.text.contexts;

import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.robotframework.ide.core.testData.text.AContextMatcher;
import org.robotframework.ide.core.testData.text.ContextType;
import org.robotframework.ide.core.testData.text.RobotTokenContext;
import org.robotframework.ide.core.testData.text.RobotTokenType;
import org.robotframework.ide.core.testData.text.TxtRobotFileLexer.TokenizatorOutput;


public class VariableTableHeaderSearcher extends AContextMatcher {

    public VariableTableHeaderSearcher(TokenizatorOutput tokenProvider) {
        super(tokenProvider);
    }


    @Override
    protected List<RobotTokenContext> findContexts(
            TokenizatorOutput tokenProvider)
            throws ConcurrentModificationException, InterruptedException,
            ExecutionException {
        ContextType type = ContextType.VARIABLES_TABLE_HEADER;

        List<List<RobotTokenType>> combinationsExpected = new LinkedList<>();
        combinationsExpected.add(Arrays
                .asList(new RobotTokenType[] { RobotTokenType.WORD_VARIABLE }));
        return buildTableHeaderContext(tokenProvider, type,
                combinationsExpected);
    }
}
