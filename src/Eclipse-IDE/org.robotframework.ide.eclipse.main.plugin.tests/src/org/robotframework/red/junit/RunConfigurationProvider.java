/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.junit;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class RunConfigurationProvider implements TestRule {

    private final String typeId;

    public RunConfigurationProvider(final String typeId) {
        this.typeId = typeId;
    }

    @Override
    public Statement apply(final Statement base, final Description description) {
        return new Statement() {

            @Override
            public void evaluate() throws Throwable {
                removeAll();
                try {
                    base.evaluate();
                } finally {
                    removeAll();
                }
            }
        };
    }

    private void removeAll() throws CoreException {
        final ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
        for (final ILaunchConfiguration config : launchManager.getLaunchConfigurations(getType())) {
            config.delete();
        }
    }

    public ILaunchConfiguration create(final String name) throws CoreException {
        return getType().newInstance(null, name);
    }

    public ILaunchConfigurationType getType() throws CoreException {
        final ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
        return launchManager.getLaunchConfigurationType(typeId);
    }

}
