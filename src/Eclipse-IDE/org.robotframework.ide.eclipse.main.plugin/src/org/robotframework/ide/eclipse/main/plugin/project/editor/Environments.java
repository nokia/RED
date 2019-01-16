/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor;

import java.util.List;

import org.rf.ide.core.environment.IRuntimeEnvironment;


/**
 * @author Michal Anglart
 *
 */
public class Environments {

    private final List<IRuntimeEnvironment> allEnvironments;

    private final IRuntimeEnvironment activeEnvironment;

    Environments(final List<IRuntimeEnvironment> allEnvironments, final IRuntimeEnvironment activeEnvironment) {
        this.allEnvironments = allEnvironments;
        this.activeEnvironment = activeEnvironment;
    }

    public IRuntimeEnvironment getActiveEnvironment() {
        return activeEnvironment;
    }

    public List<IRuntimeEnvironment> getAllEnvironments() {
        return allEnvironments;
    }

}
