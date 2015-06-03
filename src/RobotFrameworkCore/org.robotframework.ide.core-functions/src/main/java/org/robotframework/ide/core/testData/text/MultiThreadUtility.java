package org.robotframework.ide.core.testData.text;

import java.util.ConcurrentModificationException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class MultiThreadUtility<Result, Task extends Callable<Result>> {

    private final int numberOfThreads;


    public MultiThreadUtility() {
        this.numberOfThreads = Runtime.getRuntime().availableProcessors();
    }


    public List<Result> compute(List<Task> tasks) throws InterruptedException,
            ExecutionException, ConcurrentModificationException {
        List<Result> resultsComputed = new LinkedList<>();
        ExecutorService execServ = Executors
                .newFixedThreadPool(numberOfThreads);
        ExecutorCompletionService<Result> completeService = new ExecutorCompletionService<>(
                execServ);

        List<Future<Result>> futures = new LinkedList<>();

        for (Task todo : tasks) {
            Future<Result> task = completeService.submit(todo);
            futures.add(task);
        }

        try {
            for (int taskId = 0; taskId < tasks.size(); taskId++) {
                Result result = completeService.take().get();
                if (result != null) {
                    resultsComputed.add(result);
                }
            }
        } catch (InterruptedException | ExecutionException
                | ConcurrentModificationException e) {
            throw e;
        } finally {
            execServ.shutdownNow();
        }

        futures.clear();
        futures = null;

        return resultsComputed;
    }
}
