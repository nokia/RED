/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.execution.handler;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotTestExecutionService.RobotTestsLaunch;
import org.robotframework.ide.eclipse.main.plugin.views.execution.ExecutionStatusStore;
import org.robotframework.ide.eclipse.main.plugin.views.execution.ExecutionView;
import org.robotframework.ide.eclipse.main.plugin.views.execution.ExecutionViewWrapper;
import org.robotframework.ide.eclipse.main.plugin.views.execution.handler.ClearExecutionHandler.E4ClearExecutionHandler;

@SuppressWarnings("restriction")
public class ClearExecutionHandlerTest {

    @Test
    public void viewIsNotCleared_whenThereIsNoCurrentLaunch() {
        final ExecutionViewWrapper viewWrapper = mock(ExecutionViewWrapper.class);
        final ExecutionView view = mock(ExecutionView.class);

        when(viewWrapper.getComponent()).thenReturn(view);
        when(view.getCurrentlyShownLaunch()).thenReturn(Optional.empty());

        new E4ClearExecutionHandler().clearView(viewWrapper);

        verify(view, never()).clearView();
    }

    @Test
    public void viewIsNotCleared_whenThereIsCurrentLaunch_butThereIsNoExecutionStore() {
        final ExecutionViewWrapper viewWrapper = mock(ExecutionViewWrapper.class);
        final ExecutionView view = mock(ExecutionView.class);

        final RobotTestsLaunch launch = new RobotTestsLaunch(null);

        when(viewWrapper.getComponent()).thenReturn(view);
        when(view.getCurrentlyShownLaunch()).thenReturn(Optional.of(launch));

        new E4ClearExecutionHandler().clearView(viewWrapper);

        verify(view, never()).clearView();
    }

    @Test
    public void viewIsCleared_whenThereIsCurrentLaunchWithExecution() {
        final ExecutionViewWrapper viewWrapper = mock(ExecutionViewWrapper.class);
        final ExecutionView view = mock(ExecutionView.class);

        final RobotTestsLaunch launch = new RobotTestsLaunch(null);
        launch.getExecutionData(ExecutionStatusStore.class, ExecutionStatusStore::new);

        when(viewWrapper.getComponent()).thenReturn(view);
        when(view.getCurrentlyShownLaunch()).thenReturn(Optional.of(launch));

        new E4ClearExecutionHandler().clearView(viewWrapper);

        verify(view).clearView();
    }

}
