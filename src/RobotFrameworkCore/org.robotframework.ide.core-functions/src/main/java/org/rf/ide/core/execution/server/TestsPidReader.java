/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.server;

import java.util.Optional;

import org.rf.ide.core.execution.agent.RobotDefaultAgentEventListener;
import org.rf.ide.core.execution.agent.event.VersionsEvent;

public class TestsPidReader extends RobotDefaultAgentEventListener {

    private Optional<Long> pid = Optional.empty();

    public long getPid() {
        return pid.orElse(-1L);
    }

    @Override
    public void handleVersions(final VersionsEvent event) {
        this.pid = event.getPid();
    }
}
