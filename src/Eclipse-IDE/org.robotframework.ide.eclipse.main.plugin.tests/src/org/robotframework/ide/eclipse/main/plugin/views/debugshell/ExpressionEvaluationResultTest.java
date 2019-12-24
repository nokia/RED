/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.debugshell;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.Optional;

import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.rf.ide.core.execution.server.response.EvaluateExpression.ExpressionType;
import org.robotframework.ide.eclipse.main.plugin.views.debugshell.ExpressionEvaluationResult.ExpressionEvaluationResultListener;

public class ExpressionEvaluationResultTest {

    @Test
    public void listenerIsNotifiedAboutResultOfEvaluation() {
        final ExpressionEvaluationResult result = new ExpressionEvaluationResult();

        final ExpressionEvaluationResultListener listener = mock(ExpressionEvaluationResultListener.class);
        result.addListener(1, listener);

        result.evaluationEnded(1, ExpressionType.ROBOT, Optional.of("result"), Optional.empty());

        verify(listener).handleResult(1, ExpressionType.ROBOT, Optional.of("result"), Optional.empty());
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void listenerIsNotifiedAboutEvaluationEnd_whenSessionPauses() {
        final ExpressionEvaluationResult result = new ExpressionEvaluationResult();

        final ExpressionEvaluationResultListener listener = mock(ExpressionEvaluationResultListener.class);
        result.addListener(2, listener);

        result.evaluationEnded(2, ExpressionType.ROBOT, Optional.of("result"), Optional.empty());
        result.paused();

        final InOrder orderVerifier = Mockito.inOrder(listener);
        orderVerifier.verify(listener).handleResult(2, ExpressionType.ROBOT, Optional.of("result"), Optional.empty());
        orderVerifier.verify(listener).evaluatorFinished();
        orderVerifier.verifyNoMoreInteractions();
    }

    @Test
    public void listenerIsNotNotifiedAboutEvaluationEnd_whenSessionPausesButNoResultWasGiven() {
        final ExpressionEvaluationResult result = new ExpressionEvaluationResult();

        final ExpressionEvaluationResultListener listener = mock(ExpressionEvaluationResultListener.class);
        result.addListener(3, listener);

        result.paused();

        verifyZeroInteractions(listener);
    }

    @Test
    public void listenersAreNotifiedAboutEvaluationTermination_1() {
        final ExpressionEvaluationResult result = new ExpressionEvaluationResult();

        final ExpressionEvaluationResultListener listener1 = mock(ExpressionEvaluationResultListener.class);
        final ExpressionEvaluationResultListener listener2 = mock(ExpressionEvaluationResultListener.class);
        result.addListener(6, listener1);
        result.addListener(7, listener2);

        result.terminated();

        verify(listener1).evaluatorFinished();
        verify(listener2).evaluatorFinished();
        verifyNoMoreInteractions(listener1, listener2);
    }

    @Test
    public void listenersAreNotifiedAboutEvaluationTermination_2() {
        final ExpressionEvaluationResult result = new ExpressionEvaluationResult();

        final ExpressionEvaluationResultListener listener1 = mock(ExpressionEvaluationResultListener.class);
        final ExpressionEvaluationResultListener listener2 = mock(ExpressionEvaluationResultListener.class);
        result.addListener(8, listener1);
        result.addListener(9, listener2);

        result.evaluationEnded(8, ExpressionType.PYTHON, Optional.empty(), Optional.of("error"));
        result.terminated();

        verify(listener1).evaluatorFinished();
        verify(listener2).evaluatorFinished();
        verify(listener1).handleResult(8, ExpressionType.PYTHON, Optional.empty(), Optional.of("error"));
        verifyNoMoreInteractions(listener1, listener2);
    }

    @Test
    public void multipleListenersAreNotifiedAboutResultsOfDifferentEvaluations() throws InterruptedException {
        final ExpressionEvaluationResult result = new ExpressionEvaluationResult();

        final TimeKeepingListener listener1 = new TimeKeepingListener();
        final TimeKeepingListener listener2 = new TimeKeepingListener();
        final TimeKeepingListener listener3 = new TimeKeepingListener();

        result.addListener(1, listener1);
        result.addListener(2, listener2);
        result.addListener(3, listener3);

        result.evaluationEnded(2, ExpressionType.ROBOT, Optional.of("result"), Optional.empty());
        Thread.sleep(50);
        result.evaluationEnded(1, ExpressionType.VARIABLE, Optional.of("7"), Optional.empty());
        Thread.sleep(50);
        result.evaluationEnded(3, ExpressionType.PYTHON, Optional.empty(), Optional.of("error"));

        assertThat(listener2.timestamp < listener1.timestamp).isTrue();
        assertThat(listener2.timestamp < listener3.timestamp).isTrue();
        assertThat(listener1.timestamp < listener3.timestamp).isTrue();
    }

    private static class TimeKeepingListener implements ExpressionEvaluationResultListener {

        private long timestamp;

        @Override
        public void handleResult(final int id, final ExpressionType expressionType, final Optional<String> result,
                final Optional<String> error) {
            this.timestamp = System.currentTimeMillis();
        }

        @Override
        public void evaluatorFinished() {
            // nothing to do
        }
    }
}
