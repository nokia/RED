/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.debugshell;

import org.rf.ide.core.execution.agent.RobotDefaultAgentEventListener;
import org.rf.ide.core.execution.agent.event.ExpressionEvaluatedEvent;
import org.rf.ide.core.execution.agent.event.PausedEvent;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotTestExecutionService.RobotTestsLaunch;

public class ExpressionEvaluationTracker extends RobotDefaultAgentEventListener {

    private final RobotTestsLaunch testsLaunchContext;

    public ExpressionEvaluationTracker(final RobotTestsLaunch testsLaunchContext) {
        this.testsLaunchContext = testsLaunchContext;
    }

    @Override
    public void handleExpressionEvaluated(final ExpressionEvaluatedEvent event) {
        testsLaunchContext.performOnExecutionData(ExpressionEvaluationResult.class,
                tracker -> tracker.evaluationEnded(event.getId(), event.getType(), event.getResult(),
                        event.getError()));
    }

    @Override
    public void handlePaused(final PausedEvent event) {
        testsLaunchContext.performOnExecutionData(ExpressionEvaluationResult.class, ExpressionEvaluationResult::paused);
    }

    @Override
    public void eventsProcessingFinished() {
        testsLaunchContext.performOnExecutionData(ExpressionEvaluationResult.class,
                ExpressionEvaluationResult::terminated);
    }
}
