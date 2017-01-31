/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor;

import java.util.List;

import org.rf.ide.core.executor.RobotRuntimeEnvironment;

/**
 * @author Michal Anglart
 *
 */
public class Environments {

    private final List<RobotRuntimeEnvironment> allEnvironments;

    private final RobotRuntimeEnvironment activeEnvironment;

    Environments(final List<RobotRuntimeEnvironment> allEnvironments,
            final RobotRuntimeEnvironment activeEnvironment) {
        this.allEnvironments = allEnvironments;
        this.activeEnvironment = activeEnvironment;
    }

    public RobotRuntimeEnvironment getActiveEnvironment() {
        return activeEnvironment;
    }

    public List<RobotRuntimeEnvironment> getAllEnvironments() {
        return allEnvironments;
    }

}
