/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.debug.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import org.eclipse.debug.core.model.IBreakpoint;
import org.junit.Test;
import org.rf.ide.core.execution.debug.StackFrame;
import org.rf.ide.core.execution.debug.Stacktrace;
import org.rf.ide.core.execution.debug.UserProcessDebugController;
import org.rf.ide.core.execution.debug.UserProcessDebugController.DebuggerPreferences;

public class RobotThreadTest {

    @Test
    public void threadPropertiesTest() {
        final Stacktrace stacktrace = new Stacktrace();
        final StackFrame frame1 = mock(StackFrame.class);
        final StackFrame frame2 = mock(StackFrame.class);
        final StackFrame topFrame = mock(StackFrame.class);
        stacktrace.push(frame1);
        stacktrace.push(frame2);
        stacktrace.push(topFrame);

        final UserProcessDebugController userController = new UserProcessDebugController(stacktrace,
                mock(DebuggerPreferences.class));
        final RobotDebugTarget target = mock(RobotDebugTarget.class);
        when(target.isSuspended()).thenReturn(true);

        final RobotThread thread = new RobotThread(target, stacktrace, userController);
        assertThat(thread.getName()).isEqualTo("Tests execution thread");
        assertThat(thread.getPriority()).isEqualTo(0);
        assertThat(thread.getBreakpoints()).isEmpty();

        assertThat(thread.getTopStackFrame().getFrame()).isSameAs(topFrame);
        assertThat(Stream.of(thread.getStackFrames()).map(RobotStackFrame::getFrame)).containsExactly(topFrame, frame2,
                frame1);
    }

    @Test
    public void threadWithEmptyStackHasNullTopFrame() {
        final Stacktrace stacktrace = new Stacktrace();

        final UserProcessDebugController userController = new UserProcessDebugController(stacktrace,
                mock(DebuggerPreferences.class));
        final RobotDebugTarget target = mock(RobotDebugTarget.class);
        when(target.isSuspended()).thenReturn(true);

        final RobotThread thread = new RobotThread(target, stacktrace, userController);
        assertThat(thread.getTopStackFrame()).isNull();
    }

    @Test
    public void threadHasFrames_whenIsSuspendedAndStackIsNonEmpty() {
        final Stacktrace stacktrace = new Stacktrace();
        final UserProcessDebugController userController = new UserProcessDebugController(stacktrace,
                mock(DebuggerPreferences.class));
        final RobotDebugTarget target = mock(RobotDebugTarget.class);
        when(target.isSuspended()).thenReturn(true);

        final RobotThread thread = new RobotThread(target, stacktrace, userController);
        assertThat(thread.hasStackFrames()).isFalse();

        stacktrace.push(mock(StackFrame.class));
        assertThat(thread.hasStackFrames()).isTrue();
    }

    @Test
    public void threadHasNoFrames_whenIsNotSuspended() {
        final Stacktrace stacktrace = new Stacktrace();
        final UserProcessDebugController userController = new UserProcessDebugController(stacktrace,
                mock(DebuggerPreferences.class));
        final RobotDebugTarget target = mock(RobotDebugTarget.class);
        when(target.isSuspended()).thenReturn(false);

        final RobotThread thread = new RobotThread(target, stacktrace, userController);
        assertThat(thread.hasStackFrames()).isFalse();

        stacktrace.push(mock(StackFrame.class));
        assertThat(thread.hasStackFrames()).isFalse();
    }

    @Test
    public void threadKnowsBreakpointsOnWhichItIsSuspended() {
        final IBreakpoint breakpoint1 = mock(IBreakpoint.class);
        final IBreakpoint breakpoint2 = mock(IBreakpoint.class);

        final Stacktrace stacktrace = new Stacktrace();

        final UserProcessDebugController userController = new UserProcessDebugController(stacktrace,
                mock(DebuggerPreferences.class));
        final RobotDebugTarget target = mock(RobotDebugTarget.class);

        final RobotThread thread = new RobotThread(target, stacktrace, userController);
        assertThat(thread.getBreakpoints()).isEmpty();

        thread.suspendedAt(breakpoint1);
        assertThat(thread.getBreakpoints()).containsExactly(breakpoint1);

        thread.suspendedAt(breakpoint2);
        assertThat(thread.getBreakpoints()).containsExactly(breakpoint2);

        thread.resumed();
        assertThat(thread.getBreakpoints()).isEmpty();
    }

    @Test
    public void threadIsStepping_whenControllerSaysSo_1() {
        final Stacktrace stacktrace = new Stacktrace();

        final UserProcessDebugController userController = mock(UserProcessDebugController.class);
        when(userController.isStepping()).thenReturn(true);
        final RobotThread thread = new RobotThread(mock(RobotDebugTarget.class), stacktrace, userController);

        assertThat(thread.isStepping()).isTrue();
    }

    @Test
    public void threadIsStepping_whenControllerSaysSo_2() {
        final Stacktrace stacktrace = new Stacktrace();

        final UserProcessDebugController userController = mock(UserProcessDebugController.class);
        when(userController.isStepping()).thenReturn(false);
        final RobotThread thread = new RobotThread(mock(RobotDebugTarget.class), stacktrace, userController);

        assertThat(thread.isStepping()).isFalse();
    }

    @Test
    public void cannotPerformStep_whenThreadIsNotSuspended() {
        final Stacktrace stacktrace = new Stacktrace();
        final UserProcessDebugController userController = new UserProcessDebugController(stacktrace,
                mock(DebuggerPreferences.class));
        final RobotDebugTarget target = mock(RobotDebugTarget.class);
        when(target.isSuspended()).thenReturn(false);

        final RobotThread thread = new RobotThread(target, stacktrace, userController);
        assertThat(thread.canStepInto()).isFalse();
        assertThat(thread.canStepOver()).isFalse();
        assertThat(thread.canStepReturn()).isFalse();

        stacktrace.push(mock(StackFrame.class));
        assertThat(thread.canStepInto()).isFalse();
        assertThat(thread.canStepOver()).isFalse();
        assertThat(thread.canStepReturn()).isFalse();
    }

    @Test
    public void canPerformStep_whenThreadIsSuspendedAndStackIsNonEmpty() {
        final Stacktrace stacktrace = new Stacktrace();
        final UserProcessDebugController userController = new UserProcessDebugController(stacktrace,
                mock(DebuggerPreferences.class));
        final RobotDebugTarget target = mock(RobotDebugTarget.class);
        when(target.isSuspended()).thenReturn(true);

        final RobotThread thread = new RobotThread(target, stacktrace, userController);
        assertThat(thread.canStepInto()).isFalse();
        assertThat(thread.canStepOver()).isFalse();
        assertThat(thread.canStepReturn()).isFalse();

        stacktrace.push(mock(StackFrame.class));
        assertThat(thread.canStepInto()).isTrue();
        assertThat(thread.canStepOver()).isTrue();
        assertThat(thread.canStepReturn()).isTrue();
    }

    @Test
    public void stepsArePerformedThroughTopFrame() {
        final Stacktrace stacktrace = new Stacktrace();
        stacktrace.push(mock(StackFrame.class));
        
        final UserProcessDebugController userController = new UserProcessDebugController(stacktrace,
                mock(DebuggerPreferences.class));
        final RobotDebugTarget target = mock(RobotDebugTarget.class);
        when(target.isSuspended()).thenReturn(true);

        final RobotThread thread = spy(new RobotThread(target, stacktrace, userController));
        final RobotStackFrame topRobotFrame = mock(RobotStackFrame.class);
        when(thread.getTopStackFrame()).thenReturn(topRobotFrame);

        thread.stepInto();
        thread.stepOver();
        thread.stepReturn();

        verify(topRobotFrame).stepInto();
        verify(topRobotFrame).stepOver();
        verify(topRobotFrame).stepReturn();
        verifyNoMoreInteractions(topRobotFrame);
    }
}
