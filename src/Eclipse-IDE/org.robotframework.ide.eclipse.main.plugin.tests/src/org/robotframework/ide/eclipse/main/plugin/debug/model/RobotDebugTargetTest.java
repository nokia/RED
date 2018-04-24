/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.debug.model;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.function.Consumer;

import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.ILineBreakpoint;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.rf.ide.core.execution.debug.StackFrame;
import org.rf.ide.core.execution.debug.StackFrameVariable;
import org.rf.ide.core.execution.debug.Stacktrace;
import org.rf.ide.core.execution.debug.UserProcessDebugController;
import org.rf.ide.core.execution.debug.UserProcessDebugController.DebuggerPreferences;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableScope;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotDebugTarget.ExecutionPauseReasonsListener;
import org.robotframework.ide.eclipse.main.plugin.launch.IRobotProcess;

public class RobotDebugTargetTest {

    @Test
    public void propertiesTest() {
        final ILaunch launch = mock(ILaunch.class);
        final Stacktrace stack = new Stacktrace();
        final UserProcessDebugController controller = new UserProcessDebugController(stack,
                mock(DebuggerPreferences.class));
        final RobotDebugTarget target = new RobotDebugTarget("target", launch, stack, controller);

        assertThat(target.getModelIdentifier()).isEqualTo(RobotDebugElement.DEBUG_MODEL_ID);
        assertThat(target.getDebugTarget()).isSameAs(target);
        assertThat(target.getLaunch()).isSameAs(launch);
        assertThat(target.getAdapter(IDebugElement.class)).isSameAs(target);
        assertThat(target.getAdapter(ILaunch.class)).isSameAs(launch);

        assertThat(target.getProcess()).isNull();
        assertThat(target.hasThreads()).isFalse();
        assertThat(target.getThreads()).isEmpty();
        assertThat(target.getThread()).isNull();
    }

    @Test
    public void targetNameIsReturned_whenNotSuspended() {
        final ILaunch launch = mock(ILaunch.class);
        final Stacktrace stack = new Stacktrace();
        final UserProcessDebugController controller = new UserProcessDebugController(stack,
                mock(DebuggerPreferences.class));
        final IRobotProcess process = mock(IRobotProcess.class);
        when(process.isSuspended()).thenReturn(false);

        final RobotDebugTarget target = new RobotDebugTarget("target", launch, stack, controller);
        target.setProcess(process);

        assertThat(target.getName()).isEqualTo("target");
    }

    @Test
    public void targetNameWithDecorationPrefixIsReturned_whenSuspended() {
        final ILaunch launch = mock(ILaunch.class);
        final Stacktrace stack = new Stacktrace();
        final UserProcessDebugController controller = new UserProcessDebugController(stack,
                mock(DebuggerPreferences.class));
        final IRobotProcess process = mock(IRobotProcess.class);
        when(process.isSuspended()).thenReturn(true);

        final RobotDebugTarget target = new RobotDebugTarget("target", launch, stack, controller);
        target.setProcess(process);

        assertThat(target.getName()).isEqualTo("<suspended>target");
    }

    @Test
    public void processIsProperlySet() {
        final ILaunch launch = mock(ILaunch.class);
        final Stacktrace stack = new Stacktrace();
        final UserProcessDebugController controller = new UserProcessDebugController(stack,
                mock(DebuggerPreferences.class));
        final RobotDebugTarget target = new RobotDebugTarget("target", launch, stack, controller);

        final IRobotProcess process = mock(IRobotProcess.class);
        target.setProcess(process);

        assertThat(target.getProcess()).isSameAs(process);
    }

    @Test
    public void whenTargetGetsConnected_itHaveThreadCreated() {
        final ILaunch launch = mock(ILaunch.class);
        final Stacktrace stack = new Stacktrace();
        final UserProcessDebugController controller = new UserProcessDebugController(stack,
                mock(DebuggerPreferences.class));
        final RobotDebugTarget target = new RobotDebugTarget("target", launch, stack, controller);

        target.connected();

        assertThat(target.hasThreads()).isTrue();
        assertThat(target.getThread()).isNotNull();
        assertThat(target.getThreads()).containsExactly(target.getThread());
    }

    @Test
    public void suspendingIsDoneThroughProcess() {
        final ILaunch launch = mock(ILaunch.class);
        final Stacktrace stack = new Stacktrace();
        final UserProcessDebugController controller = new UserProcessDebugController(stack,
                mock(DebuggerPreferences.class));
        final IRobotProcess process = mock(IRobotProcess.class);
        when(process.canSuspend()).thenReturn(true);
        when(process.isSuspended()).thenReturn(true);

        final RobotDebugTarget target = new RobotDebugTarget("target", launch, stack, controller);
        target.setProcess(process);

        assertThat(target.canSuspend()).isTrue();
        target.suspend();
        assertThat(target.isSuspended()).isTrue();

        verify(process).suspend();
    }

    @Test
    public void resumingIsDoneThroughProcess() {
        final ILaunch launch = mock(ILaunch.class);
        final Stacktrace stack = new Stacktrace();
        final UserProcessDebugController controller = new UserProcessDebugController(stack,
                mock(DebuggerPreferences.class));
        final IRobotProcess process = mock(IRobotProcess.class);
        when(process.canResume()).thenReturn(true);
        final RobotThread thread = mock(RobotThread.class);
        @SuppressWarnings("unchecked")
        final Consumer<DebugEvent> notifier = mock(Consumer.class);

        final RobotDebugTarget target = new RobotDebugTarget("target", launch, stack, controller, notifier);
        target.setProcess(process);
        target.setThread(thread);

        assertThat(target.canResume()).isTrue();
        target.resume();

        verify(process).resume();
        verify(thread).resumed();
        verify(thread).fireResumeEvent(DebugEvent.CLIENT_REQUEST);
        verify(notifier).accept(argThat(isDebugEventOn(target, DebugEvent.CHANGE, DebugEvent.CONTENT)));
    }

    @Test
    public void disconnectingIsDoneThroughProcess() {
        final ILaunch launch = mock(ILaunch.class);
        final Stacktrace stack = new Stacktrace();
        final UserProcessDebugController controller = new UserProcessDebugController(stack,
                mock(DebuggerPreferences.class));
        final IRobotProcess process = mock(IRobotProcess.class);
        when(process.canDisconnect()).thenReturn(true);
        when(process.isDisconnected()).thenReturn(true);
        @SuppressWarnings("unchecked")
        final Consumer<DebugEvent> notifier = mock(Consumer.class);

        final RobotDebugTarget target = new RobotDebugTarget("target", launch, stack, controller, notifier);
        target.setProcess(process);
        target.setThread(mock(RobotThread.class));

        assertThat(target.canDisconnect()).isTrue();
        target.disconnect();
        assertThat(target.isDisconnected()).isTrue();

        assertThat(target.getThread()).isNull();
        verify(process).disconnect();
        verify(notifier).accept(argThat(isDebugEventOn(target, DebugEvent.TERMINATE)));
    }

    @Test
    public void terminatingIsDoneThroughProcess() throws Exception {
        final ILaunch launch = mock(ILaunch.class);
        final Stacktrace stack = new Stacktrace();
        final UserProcessDebugController controller = new UserProcessDebugController(stack,
                mock(DebuggerPreferences.class));
        final IRobotProcess process = mock(IRobotProcess.class);
        when(process.canTerminate()).thenReturn(true);
        when(process.isTerminated()).thenReturn(true);
        @SuppressWarnings("unchecked")
        final Consumer<DebugEvent> notifier = mock(Consumer.class);

        final RobotDebugTarget target = new RobotDebugTarget("target", launch, stack, controller, notifier);
        target.setProcess(process);
        target.setThread(mock(RobotThread.class));

        assertThat(target.canTerminate()).isTrue();
        target.terminate();
        assertThat(target.isTerminated()).isTrue();

        assertThat(target.getThread()).isNull();
        verify(process).terminate();
        verify(notifier).accept(argThat(isDebugEventOn(target, DebugEvent.TERMINATE)));
    }

    @Test
    public void nothingHappens_whenBreakpointsAreAddedRemovedOrChanged() {
        final ILaunch launch = mock(ILaunch.class);
        final Stacktrace stack = new Stacktrace();
        final UserProcessDebugController controller = new UserProcessDebugController(stack,
                mock(DebuggerPreferences.class));
        final RobotDebugTarget target = spy(new RobotDebugTarget("target", launch, stack, controller));

        final IBreakpoint breakpoint = mock(IBreakpoint.class);
        final IMarkerDelta delta = mock(IMarkerDelta.class);
        target.breakpointAdded(breakpoint);
        target.breakpointRemoved(breakpoint, delta);
        target.breakpointChanged(breakpoint, delta);

        verifyZeroInteractions(breakpoint, delta);

        verify(target).breakpointAdded(any(IBreakpoint.class));
        verify(target).breakpointRemoved(any(IBreakpoint.class), any(IMarkerDelta.class));
        verify(target).breakpointChanged(any(IBreakpoint.class), any(IMarkerDelta.class));
        verifyNoMoreInteractions(target);
    }

    @Test
    public void robotDebugTargetSupportsRobotBreakpoint() {
        final ILaunch launch = mock(ILaunch.class);
        final Stacktrace stack = new Stacktrace();
        final UserProcessDebugController controller = new UserProcessDebugController(stack,
                mock(DebuggerPreferences.class));
        final RobotDebugTarget target = new RobotDebugTarget("target", launch, stack, controller);

        final IBreakpoint robotBreakpoint = mock(IBreakpoint.class);
        when(robotBreakpoint.getModelIdentifier()).thenReturn(RobotDebugElement.DEBUG_MODEL_ID);
        final IBreakpoint nonRobotBreakpoint = mock(IBreakpoint.class);
        when(nonRobotBreakpoint.getModelIdentifier()).thenReturn("some_model");

        assertThat(target.supportsBreakpoint(robotBreakpoint)).isTrue();
        assertThat(target.supportsBreakpoint(nonRobotBreakpoint)).isFalse();
    }

    @Test
    public void threadIsNotified_whenSuspendedOnLineBreakpoint() {
        final ILaunch launch = mock(ILaunch.class);
        final Stacktrace stack = new Stacktrace();
        final UserProcessDebugController controller = new UserProcessDebugController(stack,
                mock(DebuggerPreferences.class));
        final RobotThread thread = mock(RobotThread.class);

        final RobotDebugTarget target = new RobotDebugTarget("target", launch, stack, controller);
        target.setThread(thread);
        target.breakpointHit(mock(ILineBreakpoint.class));

        verify(thread).suspendedAt(any(ILineBreakpoint.class));
    }

    @Test
    public void threadIsNotNotified_whenSuspendedOnUsualBreakpoint() {
        final ILaunch launch = mock(ILaunch.class);
        final Stacktrace stack = new Stacktrace();
        final UserProcessDebugController controller = new UserProcessDebugController(stack,
                mock(DebuggerPreferences.class));
        final RobotThread thread = mock(RobotThread.class);

        final RobotDebugTarget target = new RobotDebugTarget("target", launch, stack, controller);
        target.setThread(thread);
        target.breakpointHit(mock(IBreakpoint.class));

        verifyZeroInteractions(thread);
    }

    @Test
    public void variableChangeRequestAreSendThroughUserController_1() {
        final ILaunch launch = mock(ILaunch.class);
        final Stacktrace stack = new Stacktrace();
        final UserProcessDebugController controller = mock(UserProcessDebugController.class);

        final StackFrameVariable variable = new StackFrameVariable(VariableScope.GLOBAL, false, "var", "int", 10);
        final StackFrame frame = mock(StackFrame.class);

        final RobotDebugTarget target = new RobotDebugTarget("target", launch, stack, controller);
        target.changeVariable(frame, variable, newArrayList("a", "b"));

        verify(controller).changeVariable(frame, variable, newArrayList("a", "b"));
    }

    @Test
    public void variableChangeRequestAreSendThroughUserController_2() {
        final ILaunch launch = mock(ILaunch.class);
        final Stacktrace stack = new Stacktrace();
        final UserProcessDebugController controller = mock(UserProcessDebugController.class);

        final StackFrameVariable variable = new StackFrameVariable(VariableScope.GLOBAL, false, "var", "int", 10);
        final StackFrame frame = mock(StackFrame.class);

        final RobotDebugTarget target = new RobotDebugTarget("target", launch, stack, controller);
        target.changeVariableInnerValue(frame, variable, newArrayList("p1", "p2"), newArrayList("a", "b"));

        verify(controller).changeVariableInnerValue(frame, variable, newArrayList("p1", "p2"), newArrayList("a", "b"));
    }

    @Test
    public void robotDebugTargetDoesNotSupportMemoryRetrieval() {
        final ILaunch launch = mock(ILaunch.class);
        final Stacktrace stack = new Stacktrace();
        final UserProcessDebugController controller = new UserProcessDebugController(stack,
                mock(DebuggerPreferences.class));
        final RobotDebugTarget target = new RobotDebugTarget("target", launch, stack, controller);

        assertThat(target.supportsStorageRetrieval()).isFalse();
        assertThat(target.getMemoryBlock(0, 100)).isNull();
    }

    @Test
    public void executionSuspendsListenerNotifiesProcessAndThreadAboutBreakpointSuspension() {
        final Stacktrace stack = new Stacktrace();
        final UserProcessDebugController controller = new UserProcessDebugController(stack,
                mock(DebuggerPreferences.class));
        final IRobotProcess process = mock(IRobotProcess.class);
        final RobotThread thread = mock(RobotThread.class);

        final RobotDebugTarget target = new RobotDebugTarget("target", mock(ILaunch.class), stack, controller);
        target.setProcess(process);
        target.setThread(thread);

        final ExecutionPauseReasonsListener listener = target.new ExecutionPauseReasonsListener();
        listener.pausedOnBreakpoint(mock(RobotLineBreakpoint.class));
        
        verify(process).suspended();
        verify(thread).fireSuspendEvent(DebugEvent.BREAKPOINT);
        verify(thread).suspendedAt(any(RobotLineBreakpoint.class));
    }

    @Test
    public void executionSuspendsListenerNotifiesProcessAndThreadAboutUserSuspension() {
        final Stacktrace stack = new Stacktrace();
        final UserProcessDebugController controller = new UserProcessDebugController(stack,
                mock(DebuggerPreferences.class));
        final IRobotProcess process = mock(IRobotProcess.class);
        final RobotThread thread = mock(RobotThread.class);

        final RobotDebugTarget target = new RobotDebugTarget("target", mock(ILaunch.class), stack, controller);
        target.setProcess(process);
        target.setThread(thread);

        final ExecutionPauseReasonsListener listener = target.new ExecutionPauseReasonsListener();
        listener.pausedByUser();

        verify(process).suspended();
        verify(thread).fireSuspendEvent(DebugEvent.CLIENT_REQUEST);
    }

    @Test
    public void executionSuspendsListenerNotifiesProcessAndThreadAboutSteppingEndSuspension() {
        final Stacktrace stack = new Stacktrace();
        final UserProcessDebugController controller = new UserProcessDebugController(stack,
                mock(DebuggerPreferences.class));
        final IRobotProcess process = mock(IRobotProcess.class);
        final RobotThread thread = mock(RobotThread.class);

        final RobotDebugTarget target = new RobotDebugTarget("target", mock(ILaunch.class), stack, controller);
        target.setProcess(process);
        target.setThread(thread);

        final ExecutionPauseReasonsListener listener = target.new ExecutionPauseReasonsListener();
        listener.pausedByStepping();

        verify(process).suspended();
        verify(thread).fireSuspendEvent(DebugEvent.STEP_END);
    }

    @Test
    public void executionSuspendsListenerNotifiesProcessAndThreadAboutErrorSuspension() {
        final Stacktrace stack = new Stacktrace();
        final UserProcessDebugController controller = new UserProcessDebugController(stack,
                mock(DebuggerPreferences.class));
        final IRobotProcess process = mock(IRobotProcess.class);
        final RobotThread thread = mock(RobotThread.class);

        final RobotDebugTarget target = new RobotDebugTarget("target", mock(ILaunch.class), stack, controller);
        target.setProcess(process);
        target.setThread(thread);

        final ExecutionPauseReasonsListener listener = target.new ExecutionPauseReasonsListener();
        listener.pausedOnError("error'");

        verify(process).suspended();
        verify(thread).fireSuspendEvent(DebugEvent.CLIENT_REQUEST);
    }

    @Test
    public void executionSuspendsListenerNotifiesProcessAndThreadAboutVariableChangeSuspension() {
        final Stacktrace stack = new Stacktrace();
        final UserProcessDebugController controller = new UserProcessDebugController(stack,
                mock(DebuggerPreferences.class));
        final IRobotProcess process = mock(IRobotProcess.class);
        final RobotThread thread = mock(RobotThread.class);

        final RobotDebugTarget target = new RobotDebugTarget("target", mock(ILaunch.class), stack, controller);
        target.setProcess(process);
        target.setThread(thread);

        final ExecutionPauseReasonsListener listener = target.new ExecutionPauseReasonsListener();
        listener.pausedAfterVariableChange(42);

        verify(process).suspended();
        verify(thread).fireSuspendEvent(DebugEvent.EVALUATION);
    }

    private static ArgumentMatcher<DebugEvent> isDebugEventOn(final RobotDebugElement element, final int kind) {
        return isDebugEventOn(element, kind, 0);
    }

    private static ArgumentMatcher<DebugEvent> isDebugEventOn(final RobotDebugElement element, final int kind,
            final int detail) {
        return event -> event.getSource() == element && event.getKind() == kind && event.getDetail() == detail;
    }
}
