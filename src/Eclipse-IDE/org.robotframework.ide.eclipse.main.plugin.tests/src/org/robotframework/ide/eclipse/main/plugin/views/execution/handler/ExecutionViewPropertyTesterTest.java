/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.execution.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.junit.jupiter.api.Test;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotTestExecutionService;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotTestExecutionService.RobotTestsLaunch;
import org.robotframework.ide.eclipse.main.plugin.views.execution.ExecutionView;
import org.robotframework.ide.eclipse.main.plugin.views.execution.ExecutionViewWrapper;

@SuppressWarnings("restriction")
public class ExecutionViewPropertyTesterTest {

    private final ExecutionViewPropertyTester tester = new ExecutionViewPropertyTester();

    @Test
    public void exceptionIsThrown_whenReceiverIsNotWorkbenchWindow() {
        assertThatIllegalArgumentException().isThrownBy(() -> tester.test(new Object(), "property", null, true))
                .withMessage("Property tester is unable to test properties of java.lang.Object. It should be used with "
                        + IWorkbenchWindow.class.getName())
                .withNoCause();
    }

    @Test
    public void falseIsReturned_whenThereIsNoExecutionViewInActivePage() {
        final IWorkbenchPage site = mock(IWorkbenchPage.class);
        when(site.findViewReference(ExecutionView.ID)).thenReturn(null);
        final IWorkbenchWindow window = mock(IWorkbenchWindow.class);
        when(window.getActivePage()).thenReturn(site);

        final boolean testResult = tester.test(window, ExecutionViewPropertyTester.CURRENT_LAUNCH_IS_TERMINATED, null,
                false);

        assertThat(testResult).isFalse();
    }

    @Test
    public void falseIsReturned_whenExpectedValueIsAString() {
        final IWorkbenchWindow window = prepareWindow(null);

        final boolean testResult = tester.test(window,
                ExecutionViewPropertyTester.CURRENT_LAUNCH_IS_TERMINATED, null, "value");

        assertThat(testResult).isFalse();
    }

    @Test
    public void falseIsReturnedForUnknownProperty() {
        final IWorkbenchWindow window = prepareWindow(null);

        assertThat(tester.test(window, "unknown_property", null, true)).isFalse();
        assertThat(tester.test(window, "unknown_property", null, false)).isFalse();
    }

    @Test
    public void testViewHasTerminatedLaunchProperty_whenThereIsNoCurrentLaunch() {
        final IWorkbenchWindow window = prepareWindow(null);

        assertThat(viewHasTerminatedLaunch(window, true)).isFalse();
        assertThat(viewHasTerminatedLaunch(window, false)).isTrue();
    }

    @Test
    public void testViewHasTerminatedLaunchProperty_whenCurrentLaunchIsRunning() {
        final RobotTestExecutionService executionService = new RobotTestExecutionService();
        final RobotTestsLaunch runningLaunch = executionService.testExecutionStarting(null);

        final IWorkbenchWindow window = prepareWindow(runningLaunch);

        assertThat(viewHasTerminatedLaunch(window, true)).isFalse();
        assertThat(viewHasTerminatedLaunch(window, false)).isTrue();
    }

    @Test
    public void testViewHasTerminatedLaunchProperty_whenCurrentLaunchIsTerminated() {
        final RobotTestExecutionService executionService = new RobotTestExecutionService();
        final RobotTestsLaunch terminatedLaunch = executionService.testExecutionStarting(null);
        executionService.testExecutionEnded(terminatedLaunch);

        final IWorkbenchWindow window = prepareWindow(terminatedLaunch);

        assertThat(viewHasTerminatedLaunch(window, true)).isTrue();
        assertThat(viewHasTerminatedLaunch(window, false)).isFalse();
    }

    private IWorkbenchWindow prepareWindow(final RobotTestsLaunch launch) {
        final ExecutionView view = mock(ExecutionView.class);
        when(view.getCurrentlyShownLaunch()).thenReturn(Optional.ofNullable(launch));

        final ExecutionViewWrapper viewWrapper = mock(ExecutionViewWrapper.class);
        when(viewWrapper.getComponent()).thenReturn(view);
        final IViewReference viewRef = mock(IViewReference.class);
        when(viewRef.getView(false)).thenReturn(viewWrapper);
        final IWorkbenchPage site = mock(IWorkbenchPage.class);
        when(site.findViewReference(ExecutionView.ID)).thenReturn(viewRef);
        final IWorkbenchWindow window = mock(IWorkbenchWindow.class);
        when(window.getActivePage()).thenReturn(site);

        return window;
    }

    private boolean viewHasTerminatedLaunch(final IWorkbenchWindow window, final boolean expected) {
        return tester.test(window, ExecutionViewPropertyTester.CURRENT_LAUNCH_IS_TERMINATED, null, expected);
    }
}
