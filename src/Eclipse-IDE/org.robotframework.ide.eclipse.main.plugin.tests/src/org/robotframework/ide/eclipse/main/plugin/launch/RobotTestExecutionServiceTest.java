/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.Collection;
import java.util.Optional;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ui.services.IDisposable;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotTestExecutionService.RobotTestExecutionListener;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotTestExecutionService.RobotTestsLaunch;

public class RobotTestExecutionServiceTest {

    @Test
    public void thereIsNoLastLaunch_whenThereWereNoLaunches() {
        final RobotTestExecutionService service = new RobotTestExecutionService();
        
        assertThat(service.getLastLaunch().isPresent()).isFalse();
    }

    @Test
    public void thereIsLastLaunch_whenSingleTestsWerePerformed() {
        final RobotTestExecutionService service = new RobotTestExecutionService();

        final ILaunchConfiguration config = mock(ILaunchConfiguration.class);
        final RobotTestsLaunch launch = service.testExecutionStarting(config);

        assertThat(launch.getLaunchConfiguration()).isSameAs(config);
        assertThat(service.getLastLaunch().get()).isSameAs(launch);
    }

    @Test
    public void thereIsLastLaunch_whenManyTestsWerePerformed() {
        final RobotTestExecutionService service = new RobotTestExecutionService();

        for (int i = 0; i < 20; i++) {
            service.testExecutionStarting(null);
        }
        final RobotTestsLaunch launch = service.testExecutionStarting(null);

        assertThat(service.getLastLaunch().get()).isSameAs(launch);
    }

    @Test
    public void olderLaunchesAreRemovedAndDisposed_whenExecutionHistoryExceedsLimit() {
        final RobotTestExecutionService service = new RobotTestExecutionService();

        final RobotTestsLaunch oldestLaunch = service.testExecutionStarting(null);
        oldestLaunch.getExecutionData(ExecutionData.class, ExecutionData::new);
        service.testExecutionEnded(oldestLaunch);

        for (int i = 0; i < RobotTestExecutionService.LAUNCHES_HISTORY_LIMIT; i++) {
            final RobotTestsLaunch launch = service.testExecutionStarting(null);
            service.testExecutionEnded(launch);
        }
        
        final Collection<RobotTestsLaunch> launchesHistory = service.getLaunches();
        assertThat(launchesHistory).hasSize(RobotTestExecutionService.LAUNCHES_HISTORY_LIMIT);
        assertThat(launchesHistory).doesNotContain(oldestLaunch);

        final Optional<ExecutionData> oldestData = oldestLaunch.getExecutionData(ExecutionData.class);
        assertThat(oldestData.isPresent()).isFalse();
    }

    @Test
    public void listenerIsNotified_whenNewLaunchIsStarting() {
        final RobotTestExecutionListener listener = mock(RobotTestExecutionListener.class);

        final RobotTestExecutionService service = new RobotTestExecutionService();
        service.addExecutionListener(listener);

        final RobotTestsLaunch launch1 = service.testExecutionStarting(null);
        final RobotTestsLaunch launch2 = service.testExecutionStarting(null);
        service.removeExecutionListener(listener);
        service.testExecutionStarting(null);

        verify(listener).executionStarting(launch1);
        verify(listener).executionStarting(launch2);
        verifyNoMoreInteractions(listener);
    }

    private static class ExecutionData implements IDisposable {

        @Override
        public void dispose() {
            // nothing to do
        }
    }

}
