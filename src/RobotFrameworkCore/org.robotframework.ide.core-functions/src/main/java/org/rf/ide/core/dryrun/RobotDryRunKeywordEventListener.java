/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.dryrun;

import java.util.function.Consumer;

import org.rf.ide.core.execution.agent.LogLevel;
import org.rf.ide.core.execution.agent.RobotDefaultAgentEventListener;
import org.rf.ide.core.execution.agent.event.SuiteStartedEvent;

public class RobotDryRunKeywordEventListener extends RobotDefaultAgentEventListener {

    private final RobotDryRunKeywordSourceCollector dryRunKeywordSourceCollector;

    private final Consumer<String> startSuiteHandler;

    public RobotDryRunKeywordEventListener(final RobotDryRunKeywordSourceCollector dryRunKeywordSourceCollector,
            final Consumer<String> startSuiteHandler) {
        this.dryRunKeywordSourceCollector = dryRunKeywordSourceCollector;
        this.startSuiteHandler = startSuiteHandler;
    }

    @Override
    public void handleSuiteStarted(final SuiteStartedEvent event) {
        startSuiteHandler.accept(event.getName());
    }

    @Override
    public void handleMessage(final String msg, final LogLevel level) {
        if (level == LogLevel.NONE) {
            dryRunKeywordSourceCollector.collectFromMessageEvent(msg);
        }
    }
}
