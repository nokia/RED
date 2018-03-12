/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.debug;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.WorkspaceSourceContainer;
import org.junit.Test;

public class RobotSourcePathComputerDelegateTest {

    @Test
    public void thereIsAlwaysOnlyWorkspaceSourceContainerComputed() {
        final RobotSourcePathComputerDelegate delegate = new RobotSourcePathComputerDelegate();

        final ISourceContainer[] sourceContainers = delegate.computeSourceContainers(mock(ILaunchConfiguration.class),
                mock(IProgressMonitor.class));

        assertThat(sourceContainers).hasSize(1);
        assertThat(sourceContainers[0]).isInstanceOf(WorkspaceSourceContainer.class);
    }

}
