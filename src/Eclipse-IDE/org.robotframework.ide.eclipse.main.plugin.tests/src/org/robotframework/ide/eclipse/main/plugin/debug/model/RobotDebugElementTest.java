/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.debug.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.function.Consumer;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugElement;
import org.junit.Test;
import org.mockito.ArgumentMatcher;

public class RobotDebugElementTest {

    @Test
    public void propertiesTest() {
        final ILaunch launch = mock(ILaunch.class);
        final RobotDebugTarget target = mock(RobotDebugTarget.class);
        when(target.getLaunch()).thenReturn(launch);

        final RobotDebugElement element = new RobotDebugElement(target);

        assertThat(element.getModelIdentifier()).isEqualTo(RobotDebugElement.DEBUG_MODEL_ID);
        assertThat(element.getDebugTarget()).isSameAs(target);
        assertThat(element.getLaunch()).isSameAs(launch);
        assertThat(element.getAdapter(IDebugElement.class)).isSameAs(element);
        assertThat(element.getAdapter(ILaunch.class)).isSameAs(launch);
    }

    @Test
    public void creationEventIsPropagatedThroughNotifier() {
        @SuppressWarnings("unchecked")
        final Consumer<DebugEvent> notifier = mock(Consumer.class);
        final RobotDebugElement element = new RobotDebugElement(mock(RobotDebugTarget.class), notifier);
        element.fireCreationEvent();

        verify(notifier).accept(argThat(isDebugEventOn(element, DebugEvent.CREATE)));
    }

    @Test
    public void resumeEventIsPropagatedThroughNotifier() {
        @SuppressWarnings("unchecked")
        final Consumer<DebugEvent> notifier = mock(Consumer.class);
        final RobotDebugElement element = new RobotDebugElement(mock(RobotDebugTarget.class), notifier);
        element.fireResumeEvent(DebugEvent.CLIENT_REQUEST);

        verify(notifier).accept(argThat(isDebugEventOn(element, DebugEvent.RESUME, DebugEvent.CLIENT_REQUEST)));
    }

    @Test
    public void suspendEventIsPropagatedThroughNotifier() {
        @SuppressWarnings("unchecked")
        final Consumer<DebugEvent> notifier = mock(Consumer.class);
        final RobotDebugElement element = new RobotDebugElement(mock(RobotDebugTarget.class), notifier);
        element.fireSuspendEvent(DebugEvent.CLIENT_REQUEST);

        verify(notifier).accept(argThat(isDebugEventOn(element, DebugEvent.SUSPEND, DebugEvent.CLIENT_REQUEST)));
    }

    @Test
    public void changeEventIsPropagatedThroughNotifier() {
        @SuppressWarnings("unchecked")
        final Consumer<DebugEvent> notifier = mock(Consumer.class);
        final RobotDebugElement element = new RobotDebugElement(mock(RobotDebugTarget.class), notifier);
        element.fireChangeEvent(DebugEvent.CLIENT_REQUEST);

        verify(notifier).accept(argThat(isDebugEventOn(element, DebugEvent.CHANGE, DebugEvent.CLIENT_REQUEST)));
    }

    @Test
    public void terminateEventIsPropagatedThroughNotifier() {
        @SuppressWarnings("unchecked")
        final Consumer<DebugEvent> notifier = mock(Consumer.class);
        final RobotDebugElement element = new RobotDebugElement(mock(RobotDebugTarget.class), notifier);
        element.fireTerminateEvent();

        verify(notifier).accept(argThat(isDebugEventOn(element, DebugEvent.TERMINATE)));
    }

    @Test
    public void suspendingIsDoneThroughTarget() {
        final RobotDebugTarget target = mock(RobotDebugTarget.class);
        when(target.canSuspend()).thenReturn(true);
        when(target.isSuspended()).thenReturn(true);

        final RobotDebugElement element = new RobotDebugElement(target);

        assertThat(element.canSuspend()).isTrue();
        element.suspend();
        assertThat(element.isSuspended()).isTrue();

        verify(target).suspend();
    }

    @Test
    public void resumingIsDoneThroughTarget() {
        final RobotDebugTarget target = mock(RobotDebugTarget.class);
        when(target.canResume()).thenReturn(true);

        final RobotDebugElement element = new RobotDebugElement(target);

        assertThat(element.canResume()).isTrue();
        element.resume();

        verify(target).resume();
    }

    @Test
    public void disconnectingIsDoneThroughTarget() {
        final RobotDebugTarget target = mock(RobotDebugTarget.class);
        when(target.canDisconnect()).thenReturn(true);
        when(target.isDisconnected()).thenReturn(true);

        final RobotDebugElement element = new RobotDebugElement(target);

        assertThat(element.canDisconnect()).isTrue();
        element.disconnect();
        assertThat(element.isDisconnected()).isTrue();

        verify(target).disconnect();
    }

    @Test
    public void terminatingIsDoneThroughTarget() throws Exception {
        final RobotDebugTarget target = mock(RobotDebugTarget.class);
        when(target.canTerminate()).thenReturn(true);
        when(target.isTerminated()).thenReturn(true);

        final RobotDebugElement element = new RobotDebugElement(target);

        assertThat(element.canTerminate()).isTrue();
        element.terminate();
        assertThat(element.isTerminated()).isTrue();

        verify(target).terminate();
    }

    private static ArgumentMatcher<DebugEvent> isDebugEventOn(final RobotDebugElement element, final int kind) {
        return isDebugEventOn(element, kind, 0);
    }

    private static ArgumentMatcher<DebugEvent> isDebugEventOn(final RobotDebugElement element, final int kind,
            final int detail) {
        return event -> {
            return event.getSource() == element && event.getKind() == kind && event.getDetail() == detail;
        };
    }
}
