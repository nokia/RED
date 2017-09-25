/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.debug;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourcePathComputerDelegate;
import org.eclipse.debug.core.sourcelookup.containers.WorkspaceSourceContainer;

public class RobotSourcePathComputerDelegate implements ISourcePathComputerDelegate {

    @Override
    public ISourceContainer[] computeSourceContainers(final ILaunchConfiguration configuration,
            final IProgressMonitor monitor) {
        return new ISourceContainer[] { new WorkspaceSourceContainer() };
    }
}
