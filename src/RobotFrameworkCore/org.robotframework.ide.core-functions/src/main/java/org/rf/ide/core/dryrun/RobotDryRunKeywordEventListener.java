/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.dryrun;

import java.io.IOException;
import java.util.function.Consumer;

import org.rf.ide.core.execution.agent.LogLevel;
import org.rf.ide.core.execution.agent.RobotDefaultAgentEventListener;
import org.rf.ide.core.execution.agent.event.MessageEvent;
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
    public void handleMessage(final MessageEvent event) {
        if (event.getLevel() == LogLevel.NONE) {
            try {
                dryRunKeywordSourceCollector.collectFromMessageEvent(event);
            } catch (final IOException e) {
                throw new JsonMessageMapper.JsonMessageMapperException("Problem with mapping keyword source message",
                        e);
            }
        }
    }
}
