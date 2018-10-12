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

import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotTestExecutionService;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotTestExecutionService.RobotTestsLaunch;
import org.robotframework.ide.eclipse.main.plugin.views.execution.ExecutionView;
import org.robotframework.ide.eclipse.main.plugin.views.execution.ExecutionViewWrapper;

@SuppressWarnings("restriction")
public class ExecutionViewPropertyTesterTest {

    private final ExecutionViewPropertyTester tester = new ExecutionViewPropertyTester();

    @Test
    public void exceptionIsThrown_whenReceiverIsNotExecutionView() {
        assertThatIllegalArgumentException().isThrownBy(() -> tester.test(new Object(), "property", null, true))
                .withMessage("Property tester is unable to test properties of java.lang.Object. It should be used with "
                        + ExecutionViewWrapper.class.getName())
                .withNoCause();
    }

    @Test
    public void falseIsReturned_whenExpectedValueIsAString() {
        final ExecutionViewWrapper viewWrapper = mock(ExecutionViewWrapper.class);
        final ExecutionView view = mock(ExecutionView.class);
        when(viewWrapper.getComponent()).thenReturn(view);

        final boolean testResult = tester.test(viewWrapper,
                ExecutionViewPropertyTester.CURRENT_LAUNCH_IS_TERMINATED, null, "value");

        assertThat(testResult).isFalse();
    }

    @Test
    public void falseIsReturnedForUnknownProperty() {
        final ExecutionViewWrapper viewWrapper = mock(ExecutionViewWrapper.class);
        final ExecutionView view = mock(ExecutionView.class);
        when(viewWrapper.getComponent()).thenReturn(view);

        assertThat(tester.test(viewWrapper, "unknown_property", null, true)).isFalse();
        assertThat(tester.test(viewWrapper, "unknown_property", null, false)).isFalse();
    }

    @Test
    public void testViewHasTerminatedLaunchProperty() {
        final RobotTestExecutionService executionService = new RobotTestExecutionService();

        final RobotTestsLaunch terminatedLaunch = executionService.testExecutionStarting(null);
        executionService.testExecutionEnded(terminatedLaunch);

        final RobotTestsLaunch runningLaunch = executionService.testExecutionStarting(null);


        final ExecutionViewWrapper viewWrapperWithoutCurrentLaunch = mock(ExecutionViewWrapper.class);
        final ExecutionView viewWithoutCurrentLaunch = mock(ExecutionView.class);
        when(viewWrapperWithoutCurrentLaunch.getComponent()).thenReturn(viewWithoutCurrentLaunch);
        when(viewWithoutCurrentLaunch.getCurrentlyShownLaunch()).thenReturn(Optional.empty());

        final ExecutionViewWrapper viewWrapperWithRunningLaunch = mock(ExecutionViewWrapper.class);
        final ExecutionView viewWithRunningLaunch = mock(ExecutionView.class);
        when(viewWrapperWithRunningLaunch.getComponent()).thenReturn(viewWithRunningLaunch);
        when(viewWithRunningLaunch.getCurrentlyShownLaunch()).thenReturn(Optional.of(runningLaunch));

        final ExecutionViewWrapper viewWrapperWithTerminatedLaunch = mock(ExecutionViewWrapper.class);
        final ExecutionView viewWithTerminatedLaunch = mock(ExecutionView.class);
        when(viewWrapperWithTerminatedLaunch.getComponent()).thenReturn(viewWithTerminatedLaunch);
        when(viewWithTerminatedLaunch.getCurrentlyShownLaunch()).thenReturn(Optional.of(terminatedLaunch));

        assertThat(viewHasTerminatedLaunch(viewWrapperWithoutCurrentLaunch, true)).isFalse();
        assertThat(viewHasTerminatedLaunch(viewWrapperWithoutCurrentLaunch, false)).isTrue();

        assertThat(viewHasTerminatedLaunch(viewWrapperWithRunningLaunch, true)).isFalse();
        assertThat(viewHasTerminatedLaunch(viewWrapperWithRunningLaunch, false)).isTrue();

        assertThat(viewHasTerminatedLaunch(viewWrapperWithTerminatedLaunch, true)).isTrue();
        assertThat(viewHasTerminatedLaunch(viewWrapperWithTerminatedLaunch, false)).isFalse();

    }

    private boolean viewHasTerminatedLaunch(final ExecutionViewWrapper view, final boolean expected) {
        return tester.test(view, ExecutionViewPropertyTester.CURRENT_LAUNCH_IS_TERMINATED, null, expected);
    }
}
