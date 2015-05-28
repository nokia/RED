package org.robotframework.ide.core.testData.text;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.robotframework.ide.core.testData.text.TxtRobotFileLexer.TokenizatorOutput;


public class ContextsMatcher {

    public void matchContexts(TokenizatorOutput tokenizatedOutput)
            throws InterruptedException, ExecutionException {
        List<AContextMatcher> matchers = new LinkedList<>();

        List<Integer> lineNumbersInTokenStream = tokenizatedOutput
                .getStartLineTokensPosition();
        for (int lineNumber : lineNumbersInTokenStream) {
            tokenizatedOutput.getContextsPerLine()
                    .add(matchForSingleLine(matchers, tokenizatedOutput,
                            lineNumber));
        }
    }


    private List<RobotTokenContext> matchForSingleLine(
            List<AContextMatcher> matchers,
            TokenizatorOutput tokenizatedOutput, int lineNumber)
            throws InterruptedException, ExecutionException {
        List<RobotTokenContext> contextsForThisLine = new LinkedList<>();

        ExecutorService execServ = Executors.newFixedThreadPool(16);
        ExecutorCompletionService<List<RobotTokenContext>> completeService = new ExecutorCompletionService<>(
                execServ);

        List<Future<List<RobotTokenContext>>> tasks = new LinkedList<>();

        for (AContextMatcher matcher : matchers) {
            matcher.setLineToMatch(lineNumber);
            Future<List<RobotTokenContext>> task = completeService
                    .submit(matcher);
            tasks.add(task);
        }

        try {
            for (int taskId = 0; taskId < tasks.size(); taskId++) {
                List<RobotTokenContext> ctxForMatcher = completeService.take()
                        .get();
                contextsForThisLine.addAll(ctxForMatcher);
            }
        } catch (InterruptedException | ExecutionException e) {
            throw e;
        } finally {
            execServ.shutdownNow();
        }

        return contextsForThisLine;
    }
}
