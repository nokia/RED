/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.swt;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.swt.widgets.Display;
import org.junit.Test;
import org.robotframework.red.swt.SwtThread.Evaluation;

public class SwtThreadTest {

    @Test
    public void syncOperationIsPerformed_1() {
        final AtomicBoolean operationPerformed = new AtomicBoolean(false);
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                sleep(200);
                operationPerformed.set(true);
            }
        };
        SwtThread.syncExec(runnable);
        assertThat(operationPerformed.get()).isTrue();
    }

    @Test
    public void syncOperationIsPerformed_2() {
        final AtomicBoolean operationPerformed = new AtomicBoolean(false);
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                sleep(200);
                operationPerformed.set(true);
            }
        };
        SwtThread.syncExec(Display.getDefault(), runnable);
        assertThat(operationPerformed.get()).isTrue();
    }

    @Test
    public void syncEvaluationIsEvaluated_1() {
        final Evaluation<String> evaluation = new Evaluation<String>() {
            @Override
            public String runCalculation() {
                sleep(200);
                return "result";
            }
        };
        assertThat(SwtThread.syncEval(evaluation)).isEqualTo("result");
    }

    @Test
    public void syncEvaluationIsEvaluated_2() {
        final Evaluation<String> evaluation = new Evaluation<String>() {
            @Override
            public String runCalculation() {
                sleep(200);
                return "result";
            }
        };
        assertThat(SwtThread.syncEval(Display.getDefault(), evaluation)).isEqualTo("result");
    }

    @Test
    public void asyncEvaluationIsPerformedAsPromised() throws InterruptedException, ExecutionException {
        final AtomicBoolean resultIsAsExpected = new AtomicBoolean(false);
        
        final Semaphore waitForAsyncEvalQueued = new Semaphore(0);
        final Thread someThread = new Thread(new Runnable() {
            @Override
            public void run() {
                final Evaluation<String> evaluation = new Evaluation<String>() {
                    @Override
                    public String runCalculation() {
                        sleep(200);
                        return "result";
                    }
                };
                final Future<String> asyncResult = SwtThread.asyncEval(evaluation);
                try {
                    waitForAsyncEvalQueued.release();
                    resultIsAsExpected.set(asyncResult.get().equals("result"));
                } catch (InterruptedException | ExecutionException e) {
                    throw new IllegalStateException("Shouldn't be interrupted!", e);
                }
            }
        });
        someThread.start();
        waitForAsyncEvalQueued.acquire();
        execAllAwaitingMessages();
        someThread.join();
        
        assertThat(resultIsAsExpected.get()).isTrue();
    }

    @Test
    public void asyncEvaluationIsPerformedAsPromised_ifTheTimeoutWasNotReached() throws InterruptedException {
        final AtomicBoolean resultIsAsExpected = new AtomicBoolean(false);

        final Semaphore waitForAsyncEvalQueued = new Semaphore(0);
        final Thread someThread = new Thread(new Runnable() {

            @Override
            public void run() {
                final Evaluation<String> evaluation = new Evaluation<String>() {

                    @Override
                    public String runCalculation() {
                        sleep(100);
                        return "result";
                    }
                };
                final Future<String> asyncResult = SwtThread.asyncEval(Display.getDefault(), evaluation);
                try {
                    waitForAsyncEvalQueued.release();
                    resultIsAsExpected.set(asyncResult.get(10000, TimeUnit.MILLISECONDS).equals("result"));
                } catch (InterruptedException | ExecutionException e) {
                    throw new IllegalStateException("Shouldn't be interrupted!", e);
                } catch (final TimeoutException e) {
                    resultIsAsExpected.set(false);
                }
            }
        });
        someThread.start();
        waitForAsyncEvalQueued.acquire();
        execAllAwaitingMessages();
        someThread.join();

        assertThat(resultIsAsExpected.get()).isTrue();
    }

    @Test
    public void timeoutExceptionIsThrown_whenEvaluationTakesTooLong() throws InterruptedException {
        final AtomicBoolean timeoutIsReached = new AtomicBoolean(false);

        final Semaphore waitForAsyncEvalQueued = new Semaphore(0);
        final Thread someThread = new Thread(new Runnable() {

            @Override
            public void run() {
                final Evaluation<String> evaluation = new Evaluation<String>() {

                    @Override
                    public String runCalculation() {
                        sleep(1000);
                        return "result";
                    }
                };
                final Future<String> asyncResult = SwtThread.asyncEval(Display.getDefault(), evaluation);
                try {
                    waitForAsyncEvalQueued.release();
                    asyncResult.get(200, TimeUnit.MILLISECONDS);
                } catch (InterruptedException | ExecutionException e) {
                    throw new IllegalStateException("Shouldn't be interrupted!", e);
                } catch (final TimeoutException e) {
                    timeoutIsReached.set(true);
                }
            }
        });
        someThread.start();
        waitForAsyncEvalQueued.acquire();
        execAllAwaitingMessages();
        someThread.join();

        assertThat(timeoutIsReached.get()).isTrue();
    }

    @Test
    public void asyncOperationIsPerformed_1() throws InterruptedException {
        final AtomicBoolean operationPerformed = new AtomicBoolean(false);

        final Semaphore waitForAsyncEvalQueued = new Semaphore(0);
        final Thread someThread = new Thread(new Runnable() {
            @Override
            public void run() {
                SwtThread.asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        sleep(200);
                        operationPerformed.set(true);
                    }
                });
                waitForAsyncEvalQueued.release();
            }
        });
        someThread.start();
        waitForAsyncEvalQueued.acquire();
        execAllAwaitingMessages();
        someThread.join();

        assertThat(operationPerformed.get()).isTrue();
    }

    @Test
    public void asyncOperationIsPerformed_2() throws InterruptedException {
        final AtomicBoolean operationPerformed = new AtomicBoolean(false);

        final Semaphore waitForAsyncEvalQueued = new Semaphore(0);
        final Thread someThread = new Thread(new Runnable() {
            @Override
            public void run() {
                SwtThread.asyncExec(Display.getDefault(), new Runnable() {
                    @Override
                    public void run() {
                        sleep(200);
                        operationPerformed.set(true);
                    }
                });
                waitForAsyncEvalQueued.release();
            }
        });
        someThread.start();
        waitForAsyncEvalQueued.acquire();
        execAllAwaitingMessages();
        someThread.join();

        assertThat(operationPerformed.get()).isTrue();
    }

    private static void execAllAwaitingMessages() {
        while (Display.getDefault().readAndDispatch()) {
            ;
        }
    }

    private static void sleep(final long millis) {
        try {
            Thread.sleep(millis);
        } catch (final InterruptedException e) {
            throw new IllegalStateException("Shouldn't be interrupted!", e);
        }
    }
}
