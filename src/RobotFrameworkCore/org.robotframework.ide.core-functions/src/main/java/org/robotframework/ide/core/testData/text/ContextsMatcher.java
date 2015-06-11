package org.robotframework.ide.core.testData.text;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.robotframework.ide.core.testData.text.TxtRobotFileLexer.TokenizatorOutput;
import org.robotframework.ide.core.testData.text.contexts.CommentsSearcher;
import org.robotframework.ide.core.testData.text.contexts.KeywordsTableHeaderSearcher;
import org.robotframework.ide.core.testData.text.contexts.PipeLineSeparatorSearcher;
import org.robotframework.ide.core.testData.text.contexts.SettingsTableHeaderSearcher;
import org.robotframework.ide.core.testData.text.contexts.TestCaseTableHeaderSearcher;
import org.robotframework.ide.core.testData.text.contexts.VariableTableHeaderSearcher;


public class ContextsMatcher {

    private final MultiThreadUtility<List<RobotTokenContext>, AContextMatcher> multiThread = new MultiThreadUtility<>();


    public void matchContexts(TokenizatorOutput tokenizatedOutput)
            throws InterruptedException, ExecutionException {
        List<RobotTokenContext> contexts = new LinkedList<>();
        List<AContextMatcher> matchers = new LinkedList<>();
        matchers.add(new SettingsTableHeaderSearcher(tokenizatedOutput));
        matchers.add(new VariableTableHeaderSearcher(tokenizatedOutput));
        matchers.add(new TestCaseTableHeaderSearcher(tokenizatedOutput));
        matchers.add(new KeywordsTableHeaderSearcher(tokenizatedOutput));

        List<List<RobotTokenContext>> computed = multiThread.compute(matchers);
        for (List<RobotTokenContext> context : computed) {
            contexts.addAll(context);
        }

        matchers.clear();
        matchers.add(new CommentsSearcher(tokenizatedOutput));
        matchers.add(new PipeLineSeparatorSearcher(tokenizatedOutput));
        computed = multiThread.compute(matchers);
        for (List<RobotTokenContext> context : computed) {
            contexts.addAll(context);
        }
        // merge contexts per line
        // temp solution adding it to one
        tokenizatedOutput.getContextsPerLine().add(contexts);
    }
}
