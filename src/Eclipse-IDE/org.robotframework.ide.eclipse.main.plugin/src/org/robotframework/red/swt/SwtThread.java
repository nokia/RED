/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.swt;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import org.eclipse.swt.widgets.Display;

/**
 * @author Michal Anglart
 *
 */
public class SwtThread {

    public static boolean isSwtThread() {
        return Display.getCurrent() != null;
    }

    public static void syncExec(final Runnable runnable) {
        syncExec(Display.getDefault(), runnable);
    }

    public static void syncExec(final Display display, final Runnable runnable) {
        display.syncExec(runnable);
    }

    public static <T> T syncEval(final Evaluation<T> evaluation) {
        return syncEval(Display.getDefault(), evaluation);
    }

    public static <T> T syncEval(final Display display, final Evaluation<T> evaluation) {
        display.syncExec(evaluation);
        return evaluation.result;
    }

    public static void asyncExec(final Runnable runnable) {
        asyncExec(Display.getDefault(), runnable);
    }

    public static void asyncExec(final Display display, final Runnable runnable) {
        display.asyncExec(runnable);
    }

    public static <T> Future<T> asyncEval(final Evaluation<T> evaluation) {
        return asyncEval(Display.getDefault(), evaluation);
    }

    public static <T> Future<T> asyncEval(final Display display, final Evaluation<T> evaluation) {
        display.asyncExec(evaluation);
        return new EvaluationPromise<>(evaluation);
    }

    public abstract static class Evaluation<T> implements Runnable {

        public static <T> Evaluation<T> of(final Supplier<T> resultsSupplier) {
            return new Evaluation<T>() {

                @Override
                public T runCalculation() {
                    return resultsSupplier.get();
                }
            };
        }

        private T result = null;

        private boolean isDone = false;

        @Override
        public final void run() {
            result = runCalculation();
            isDone = true;
        }

        public abstract T runCalculation();
    }

    private static class EvaluationPromise<T> implements Future<T> {

        private final Evaluation<T> evaluation;

        public EvaluationPromise(final Evaluation<T> evaluation) {
            this.evaluation = evaluation;
        }

        @Override
        public boolean cancel(final boolean mayInterruptIfRunning) {
            return false;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            return evaluation.isDone;
        }

        @Override
        public T get() throws InterruptedException, ExecutionException {
            while (!isDone()) {
                Thread.sleep(500);
            }
            return evaluation.result;
        }

        @Override
        public T get(final long timeout, final TimeUnit unit)
                throws InterruptedException, ExecutionException, TimeoutException {
            final long timeoutInMs = TimeUnit.MILLISECONDS.convert(timeout, unit);
            final long start = System.currentTimeMillis();

            while (timeoutIsNotReached(start, timeoutInMs)) {
                if (isDone()) {
                    return evaluation.result;
                }
                Thread.sleep(500);
            }
            throw new TimeoutException("Timeout has been reached");
        }

        private boolean timeoutIsNotReached(final long start, final long timeout) {
            final long current = System.currentTimeMillis();
            return current - start < timeout;
        }

    }
}
