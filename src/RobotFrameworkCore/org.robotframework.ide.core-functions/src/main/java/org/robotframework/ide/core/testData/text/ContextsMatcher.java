package org.robotframework.ide.core.testData.text;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.robotframework.ide.core.testData.text.TxtRobotFileLexer.TokenizatorOutput;
import org.robotframework.ide.core.testData.text.contexts.KeywordsTableHeaderSearcher;
import org.robotframework.ide.core.testData.text.contexts.SettingsTableHeaderSearcher;
import org.robotframework.ide.core.testData.text.contexts.TestCaseTableHeaderSearcher;
import org.robotframework.ide.core.testData.text.contexts.VariableTableHeaderSearcher;


public class ContextsMatcher {

    private int numberOfThreads = 16;


    public void setNumberOfThreads(int numberOfThreads) {
        if (numberOfThreads > 0) {
            this.numberOfThreads = numberOfThreads;
        }
    }


    public void matchContexts(TokenizatorOutput tokenizatedOutput)
            throws InterruptedException, ExecutionException {
        List<RobotTokenContext> contexts = new LinkedList<>();
        List<AContextMatcher> matchers = new LinkedList<>();
        matchers.add(new SettingsTableHeaderSearcher(tokenizatedOutput));
        matchers.add(new VariableTableHeaderSearcher(tokenizatedOutput));
        matchers.add(new TestCaseTableHeaderSearcher(tokenizatedOutput));
        matchers.add(new KeywordsTableHeaderSearcher(tokenizatedOutput));

        ExecutorService execServ = Executors
                .newFixedThreadPool(numberOfThreads);
        ExecutorCompletionService<List<RobotTokenContext>> completeService = new ExecutorCompletionService<>(
                execServ);

        List<Future<List<RobotTokenContext>>> tasks = new LinkedList<>();

        for (AContextMatcher matcher : matchers) {
            Future<List<RobotTokenContext>> task = completeService
                    .submit(matcher);
            tasks.add(task);
        }

        try {
            for (int taskId = 0; taskId < tasks.size(); taskId++) {
                List<RobotTokenContext> ctxForMatcher = completeService.take()
                        .get();
                contexts.addAll(ctxForMatcher);
            }
        } catch (InterruptedException | ExecutionException e) {
            throw e;
        } finally {
            execServ.shutdownNow();
        }

        // merge contexts per line
        // temp solution adding it to one
        List<RobotTokenContext> oneLineTemp = new LinkedList<>();
        oneLineTemp.addAll(contexts);
        tokenizatedOutput.getContextsPerLine().add(oneLineTemp);
    }
}
