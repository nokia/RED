/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.debugshell;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.Optional;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.junit.Test;
import org.rf.ide.core.execution.agent.event.ExpressionEvaluatedEvent;
import org.rf.ide.core.execution.agent.event.PausedEvent;
import org.rf.ide.core.execution.server.response.EvaluateExpression.ExpressionType;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotTestExecutionService.RobotTestsLaunch;

public class ExpressionEvaluationTrackerTest {

    @Test
    public void resultIsNotified_whenExpressionIsEvaluated() {
        final RobotTestsLaunch context = new RobotTestsLaunch(mock(ILaunchConfiguration.class));
        final ExpressionEvaluationResult result = context.getExecutionData(ExpressionEvaluationResult.class,
                () -> spy(new ExpressionEvaluationResult()));

        final ExpressionEvaluationTracker tracker = new ExpressionEvaluationTracker(context);
        tracker.handleExpressionEvaluated(new ExpressionEvaluatedEvent(1, ExpressionType.ROBOT, "result", null));

        verify(result).evaluationEnded(1, ExpressionType.ROBOT, Optional.of("result"), Optional.empty());
        verifyNoMoreInteractions(result);
    }

    @Test
    public void resultIsNotified_whenExpressionIsEvaluatedErroneously() {
        final RobotTestsLaunch context = new RobotTestsLaunch(mock(ILaunchConfiguration.class));
        final ExpressionEvaluationResult result = context.getExecutionData(ExpressionEvaluationResult.class,
                () -> spy(new ExpressionEvaluationResult()));

        final ExpressionEvaluationTracker tracker = new ExpressionEvaluationTracker(context);
        tracker.handleExpressionEvaluated(new ExpressionEvaluatedEvent(2, ExpressionType.ROBOT, null, "error"));

        verify(result).evaluationEnded(2, ExpressionType.ROBOT, Optional.empty(), Optional.of("error"));
        verifyNoMoreInteractions(result);
    }

    @Test
    public void resultIsNotified_whenSessionPauses() {
        final RobotTestsLaunch context = new RobotTestsLaunch(mock(ILaunchConfiguration.class));
        final ExpressionEvaluationResult result = context.getExecutionData(ExpressionEvaluationResult.class,
                () -> spy(new ExpressionEvaluationResult()));

        final ExpressionEvaluationTracker tracker = new ExpressionEvaluationTracker(context);
        tracker.handlePaused(new PausedEvent(null));

        verify(result).paused();
        verifyNoMoreInteractions(result);
    }

    @Test
    public void resultIsNotified_whenSessionTerminates() {
        final RobotTestsLaunch context = new RobotTestsLaunch(mock(ILaunchConfiguration.class));
        final ExpressionEvaluationResult result = context.getExecutionData(ExpressionEvaluationResult.class,
                () -> spy(new ExpressionEvaluationResult()));

        final ExpressionEvaluationTracker tracker = new ExpressionEvaluationTracker(context);
        tracker.eventsProcessingFinished();

        verify(result).terminated();
        verifyNoMoreInteractions(result);
    }

}
