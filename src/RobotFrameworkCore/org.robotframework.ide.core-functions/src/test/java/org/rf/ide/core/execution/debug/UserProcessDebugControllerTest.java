/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.execution.debug;

import static com.google.common.base.Predicates.not;
import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assume.assumeTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.EnumSet;
import java.util.Optional;
import java.util.concurrent.FutureTask;

import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.rf.ide.core.execution.agent.PausingPoint;
import org.rf.ide.core.execution.agent.event.ConditionEvaluatedEvent;
import org.rf.ide.core.execution.debug.StackFrame.FrameCategory;
import org.rf.ide.core.execution.debug.UserProcessController.ResponseWithCallback;
import org.rf.ide.core.execution.debug.UserProcessDebugController.DebuggerPreferences;
import org.rf.ide.core.execution.debug.UserProcessDebugController.PauseReasonListener;
import org.rf.ide.core.execution.debug.UserProcessDebugController.SteppingMode;
import org.rf.ide.core.execution.debug.UserProcessDebugController.SuspendReason;
import org.rf.ide.core.execution.debug.UserProcessDebugController.SuspensionData;
import org.rf.ide.core.execution.debug.contexts.KeywordContext;
import org.rf.ide.core.execution.server.response.ChangeVariable;
import org.rf.ide.core.execution.server.response.DisconnectExecution;
import org.rf.ide.core.execution.server.response.EvaluateCondition;
import org.rf.ide.core.execution.server.response.PauseExecution;
import org.rf.ide.core.execution.server.response.ResumeExecution;
import org.rf.ide.core.execution.server.response.ServerResponse;
import org.rf.ide.core.execution.server.response.TerminateExecution;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableScope;

@RunWith(Theories.class)
public class UserProcessDebugControllerTest {

    @DataPoints
    public static PausingPoint[] pausingPoints() {
        return EnumSet.allOf(PausingPoint.class).toArray(new PausingPoint[0]);
    }

    @Test
    public void suspensionDataIsCleared_whenConditionHasBeenEvaluatedToFalse() {
        final Stacktrace stack = new Stacktrace();
        final DebuggerPreferences prefs = new DebuggerPreferences(() -> false, true);

        final UserProcessDebugController controller = new UserProcessDebugController(stack, prefs);
        controller.setSuspensionData(new SuspensionData(SuspendReason.USER_REQUEST));
        assertThat(controller.getSuspensionData()).isNotNull();

        controller.conditionEvaluated(new ConditionEvaluatedEvent(false));
        
        assertThat(controller.getSuspensionData()).isNull();
    }

    @Test
    public void suspensionDataIsNotCleared_whenConditionHasBeenEvaluatedToTrue() {
        final Stacktrace stack = new Stacktrace();
        final DebuggerPreferences prefs = new DebuggerPreferences(() -> false, true);

        final UserProcessDebugController controller = new UserProcessDebugController(stack, prefs);
        controller.setSuspensionData(new SuspensionData(SuspendReason.USER_REQUEST));
        assertThat(controller.getSuspensionData()).isNotNull();

        controller.conditionEvaluated(new ConditionEvaluatedEvent(true));

        assertThat(controller.getSuspensionData()).isNotNull();
    }

    @Test
    public void suspensionDataIsNotCleared_whenConditionWasNotEvaluatedDueToError() {
        final Stacktrace stack = new Stacktrace();
        final DebuggerPreferences prefs = new DebuggerPreferences(() -> false, true);

        final UserProcessDebugController controller = new UserProcessDebugController(stack, prefs);
        controller.setSuspensionData(new SuspensionData(SuspendReason.USER_REQUEST));
        assertThat(controller.getSuspensionData()).isNotNull();

        controller.conditionEvaluated(new ConditionEvaluatedEvent("error"));

        assertThat(controller.getSuspensionData()).isNotNull();
    }

    @Test
    public void whenExecutionPausesForAnyReason_framesAreUnmarkedWithSteppingFlagAndSuspensionDataIsCleared() {
        final Stacktrace stack = new Stacktrace();
        stack.push(new StackFrame("a", FrameCategory.SUITE, 0, mock(StackFrameContext.class)));
        stack.push(new StackFrame("b", FrameCategory.TEST, 1, mock(StackFrameContext.class)));
        stack.push(new StackFrame("c", FrameCategory.KEYWORD, 2, mock(StackFrameContext.class)));

        stack.forEach(f -> f.mark(StackFrameMarker.STEPPING));

        final DebuggerPreferences prefs = new DebuggerPreferences(() -> false, true);

        final UserProcessDebugController controller = new UserProcessDebugController(stack, prefs);
        controller.setSuspensionData(new SuspensionData(null));

        assertThat(controller.getSuspensionData()).isNotNull();
        assertThat(stack).allMatch(f -> f.isMarkedStepping());

        controller.executionPaused();

        assertThat(controller.getSuspensionData()).isNull();
        assertThat(stack).allMatch(f -> !f.isMarkedStepping());
    }

    @Test
    public void whenExecutionPausesWithControllerBeingInBreakpointState_listenersAreNotified() {
        final Stacktrace stack = new Stacktrace();
        final DebuggerPreferences prefs = new DebuggerPreferences(() -> false, true);

        final PauseReasonListener listener1 = mock(PauseReasonListener.class);
        final PauseReasonListener listener2 = mock(PauseReasonListener.class);

        final RobotLineBreakpoint breakpoint = mock(RobotLineBreakpoint.class);
        final UserProcessDebugController controller = new UserProcessDebugController(stack, prefs);
        controller.whenSuspended(listener1);
        controller.whenSuspended(listener2);
        controller.setSuspensionData(new SuspensionData(SuspendReason.BREAKPOINT, breakpoint));

        controller.executionPaused();

        verify(listener1).pausedOnBreakpoint(breakpoint);
        verify(listener2).pausedOnBreakpoint(breakpoint);
        verifyNoMoreInteractions(listener1, listener2);
    }

    @Test
    public void whenExecutionPausesWithControllerBeingInUserRequestState_listenersAreNotified() {
        final Stacktrace stack = new Stacktrace();
        final DebuggerPreferences prefs = new DebuggerPreferences(() -> false, true);

        final PauseReasonListener listener1 = mock(PauseReasonListener.class);
        final PauseReasonListener listener2 = mock(PauseReasonListener.class);

        final UserProcessDebugController controller = new UserProcessDebugController(stack, prefs);
        controller.whenSuspended(listener1);
        controller.whenSuspended(listener2);
        controller.setSuspensionData(new SuspensionData(SuspendReason.USER_REQUEST));

        controller.executionPaused();

        verify(listener1).pausedByUser();
        verify(listener2).pausedByUser();
        verifyNoMoreInteractions(listener1, listener2);
    }

    @Test
    public void whenExecutionPausesWithControllerBeingInSteppingState_listenersAreNotified() {
        final Stacktrace stack = new Stacktrace();
        final DebuggerPreferences prefs = new DebuggerPreferences(() -> false, true);

        final PauseReasonListener listener1 = mock(PauseReasonListener.class);
        final PauseReasonListener listener2 = mock(PauseReasonListener.class);

        final UserProcessDebugController controller = new UserProcessDebugController(stack, prefs);
        controller.whenSuspended(listener1);
        controller.whenSuspended(listener2);
        controller.setSuspensionData(new SuspensionData(SuspendReason.STEPPING));

        assertThat(controller.isStepping()).isTrue();

        controller.executionPaused();

        assertThat(controller.isStepping()).isFalse();

        verify(listener1).pausedByStepping();
        verify(listener2).pausedByStepping();
        verifyNoMoreInteractions(listener1, listener2);
    }

    @Test
    public void whenExecutionPausesWithControllerBeingInVariableChangeState_listenersAreNotified() {
        final Stacktrace stack = new Stacktrace();
        final DebuggerPreferences prefs = new DebuggerPreferences(() -> false, true);

        final PauseReasonListener listener1 = mock(PauseReasonListener.class);
        final PauseReasonListener listener2 = mock(PauseReasonListener.class);

        final UserProcessDebugController controller = new UserProcessDebugController(stack, prefs);
        controller.whenSuspended(listener1);
        controller.whenSuspended(listener2);
        controller.setSuspensionData(new SuspensionData(SuspendReason.VARIABLE_CHANGE, 3));

        controller.executionPaused();

        verify(listener1).pausedAfterVariableChange(3);
        verify(listener2).pausedAfterVariableChange(3);
        verifyNoMoreInteractions(listener1, listener2);
    }

    @Test
    public void whenExecutionPausesWithControllerBeingErrorDetectedState_listenersAreNotified() {
        final Stacktrace stack = new Stacktrace();
        final DebuggerPreferences prefs = new DebuggerPreferences(() -> false, true);

        final PauseReasonListener listener1 = mock(PauseReasonListener.class);
        final PauseReasonListener listener2 = mock(PauseReasonListener.class);

        final UserProcessDebugController controller = new UserProcessDebugController(stack, prefs);
        controller.whenSuspended(listener1);
        controller.whenSuspended(listener2);
        controller.setSuspensionData(new SuspensionData(SuspendReason.ERRONEOUS_STATE, "error"));
        controller.executionPaused();

        verify(listener1).pausedOnError("error");
        verify(listener2).pausedOnError("error");
        verifyNoMoreInteractions(listener1, listener2);
    }

    @Test
    public void thereIsNoResponse_forNewlyCreatedController() {
        final Stacktrace stack = new Stacktrace();
        final DebuggerPreferences prefs = new DebuggerPreferences(() -> false, true);
        final UserProcessDebugController controller = new UserProcessDebugController(stack, prefs);

        final Optional<ServerResponse> response = controller.takeCurrentResponse(PausingPoint.START_KEYWORD);
        assertThat(response).isEmpty();
    }

    @Test
    public void whenDisconnectWasOrdered_callbackRunsAndResponseIsProperlyReturned() {
        final Stacktrace stack = new Stacktrace();
        final DebuggerPreferences prefs = new DebuggerPreferences(() -> false, true);
        final UserProcessDebugController controller = new UserProcessDebugController(stack, prefs);
        final Runnable callback = mock(Runnable.class);
        controller.disconnect(callback);

        assertThat(controller.manualUserResponse).hasSize(1);

        final Optional<ServerResponse> response = controller.takeCurrentResponse(PausingPoint.START_KEYWORD);

        assertThat(controller.manualUserResponse).isEmpty();
        assertThat(response).containsInstanceOf(DisconnectExecution.class);
        verify(callback).run();
    }

    @Test
    public void whenTerminateWasOrdered_callbackRunsAndResponseIsProperlyReturned() {
        final Stacktrace stack = new Stacktrace();
        final DebuggerPreferences prefs = new DebuggerPreferences(() -> false, true);
        final UserProcessDebugController controller = new UserProcessDebugController(stack, prefs);
        final Runnable callback = mock(Runnable.class);
        controller.terminate(callback);

        assertThat(controller.manualUserResponse).hasSize(1);

        final Optional<ServerResponse> response = controller.takeCurrentResponse(PausingPoint.START_KEYWORD);

        assertThat(controller.manualUserResponse).isEmpty();
        assertThat(response).containsInstanceOf(TerminateExecution.class);
        verify(callback).run();
    }

    @Test
    public void whenPauseWasOrdered_callbackRunsAndResponseIsProperlyReturned() {
        final Stacktrace stack = new Stacktrace();
        final DebuggerPreferences prefs = new DebuggerPreferences(() -> false, true);
        final UserProcessDebugController controller = new UserProcessDebugController(stack, prefs);
        final Runnable callback = mock(Runnable.class);
        controller.pause(callback);

        assertThat(controller.manualUserResponse).hasSize(1);

        final Optional<ServerResponse> response = controller.takeCurrentResponse(PausingPoint.START_KEYWORD);

        assertThat(controller.getSuspensionData().reason).isEqualTo(SuspendReason.USER_REQUEST);
        assertThat(controller.getSuspensionData().data).isEmpty();
        assertThat(controller.manualUserResponse).isEmpty();
        assertThat(response).containsInstanceOf(PauseExecution.class);
        verify(callback).run();
    }

    @Test
    public void whenResumeWasOrdered_callbackRunsAndResponseIsProperlyReturned() {
        final Stacktrace stack = new Stacktrace();
        final DebuggerPreferences prefs = new DebuggerPreferences(() -> false, true);
        final UserProcessDebugController controller = new UserProcessDebugController(stack, prefs);
        final Runnable callback = mock(Runnable.class);
        controller.resume(callback);

        assertThat(controller.manualUserResponse).hasSize(1);

        final Optional<ServerResponse> response = controller.takeCurrentResponse(PausingPoint.START_KEYWORD);

        assertThat(controller.manualUserResponse).isEmpty();
        assertThat(response).containsInstanceOf(ResumeExecution.class);
        verify(callback).run();
    }

    @Theory
    public void pauseResponseIsReturned_whenThereIsAnErroneousFrameAndPauseOnErrorIsEnabled(
            final PausingPoint pausingPoint) {
        assumeTrue(pausingPoint == PausingPoint.PRE_START_KEYWORD || pausingPoint == PausingPoint.START_KEYWORD);

        final Stacktrace stack = new Stacktrace();
        stack.push(new StackFrame("Suite", FrameCategory.SUITE, 0, context()));
        stack.push(new StackFrame("Test", FrameCategory.TEST, 1, context()));
        stack.push(new StackFrame("keyword", FrameCategory.KEYWORD, 2, erroneousContext("error msg")));

        final DebuggerPreferences prefs = new DebuggerPreferences(() -> true, true);
        final UserProcessDebugController controller = new UserProcessDebugController(stack, prefs);

        final Optional<ServerResponse> response = controller.takeCurrentResponse(pausingPoint);

        assertThat(response).isNotEmpty().containsInstanceOf(PauseExecution.class);
        assertThat(stack.stream().filter(StackFrame::isErroneous)).allMatch(StackFrame::isMarkedError);
        assertThat(controller.getSuspensionData().reason).isEqualByComparingTo(SuspendReason.ERRONEOUS_STATE);
        assertThat(controller.getSuspensionData().data).containsOnly("error msg");
    }

    @Theory
    public void noResponseIsReturned_whenThereIsAnErroneousFramePauseOnErrorIsEnabledButPausingPointIsAtTheEnd(
            final PausingPoint pausingPoint) {
        assumeTrue(pausingPoint == PausingPoint.PRE_END_KEYWORD || pausingPoint == PausingPoint.END_KEYWORD);

        final Stacktrace stack = new Stacktrace();
        stack.push(new StackFrame("Suite", FrameCategory.SUITE, 0, context()));
        stack.push(new StackFrame("Test", FrameCategory.TEST, 1, context()));
        stack.push(new StackFrame("keyword", FrameCategory.KEYWORD, 2, erroneousContext("error msg")));

        final DebuggerPreferences prefs = new DebuggerPreferences(() -> true, true);
        final UserProcessDebugController controller = new UserProcessDebugController(stack, prefs);

        final Optional<ServerResponse> response = controller.takeCurrentResponse(pausingPoint);

        assertThat(response).isEmpty();
        assertThat(stack.stream().filter(StackFrame::isErroneous)).allMatch(not(StackFrame::isMarkedError));
        assertThat(controller.getSuspensionData()).isNull();
    }

    @Test
    public void noResponseIsReturned_whenThereIsAnErroneousFrameButPauseOnErrorIsDisabledAndFrameGetsMarked() {
        final Stacktrace stack = new Stacktrace();
        stack.push(new StackFrame("Suite", FrameCategory.SUITE, 0, context()));
        stack.push(new StackFrame("Test", FrameCategory.TEST, 1, context()));
        stack.push(new StackFrame("keyword", FrameCategory.KEYWORD, 2, erroneousContext("error msg")));

        final DebuggerPreferences prefs = new DebuggerPreferences(() -> false, true);
        final UserProcessDebugController controller = new UserProcessDebugController(stack, prefs);

        final Optional<ServerResponse> response = controller.takeCurrentResponse(PausingPoint.START_KEYWORD);

        assertThat(response).isEmpty();
        assertThat(stack.stream().filter(StackFrame::isErroneous)).allMatch(StackFrame::isMarkedError);
        assertThat(controller.getSuspensionData()).isNull();
    }

    @Test
    public void noResponseIsReturned_whenThereIsAnErroneousFrameAndPauseOnErrorIsEnabledButFrameIsAlreadyMarked() {
        final Stacktrace stack = new Stacktrace();
        stack.push(new StackFrame("Suite", FrameCategory.SUITE, 0, context()));
        stack.push(new StackFrame("Test", FrameCategory.TEST, 1, context()));
        stack.push(new StackFrame("keyword", FrameCategory.KEYWORD, 2, erroneousContext("error msg")));
        stack.peekCurrentFrame().get().mark(StackFrameMarker.ERROR);

        final DebuggerPreferences prefs = new DebuggerPreferences(() -> true, true);
        final UserProcessDebugController controller = new UserProcessDebugController(stack, prefs);

        final Optional<ServerResponse> response = controller.takeCurrentResponse(PausingPoint.START_KEYWORD);

        assertThat(response).isEmpty();
        assertThat(stack.stream().filter(StackFrame::isErroneous)).allMatch(StackFrame::isMarkedError);
        assertThat(controller.getSuspensionData()).isNull();
    }

    @Test
    public void pauseResponseIsReturned_whenThereIsABreakpointWithoutConditionWithHitCountFulfilled() {
        final RobotLineBreakpoint breakpoint = mock(RobotLineBreakpoint.class);
        when(breakpoint.evaluateHitCount()).thenReturn(true);
        when(breakpoint.isConditionEnabled()).thenReturn(false);

        final Stacktrace stack = new Stacktrace();
        stack.push(new StackFrame("Suite", FrameCategory.SUITE, 0, context()));
        stack.push(new StackFrame("Test", FrameCategory.TEST, 1, context()));
        stack.push(new StackFrame("keyword", FrameCategory.KEYWORD, 2, breakpointContext(breakpoint)));

        final DebuggerPreferences prefs = new DebuggerPreferences(() -> false, true);
        final UserProcessDebugController controller = new UserProcessDebugController(stack, prefs);

        final Optional<ServerResponse> response = controller.takeCurrentResponse(PausingPoint.PRE_START_KEYWORD);

        assertThat(response).isNotEmpty().containsInstanceOf(PauseExecution.class);
        assertThat(controller.getSuspensionData().reason).isEqualByComparingTo(SuspendReason.BREAKPOINT);
        assertThat(controller.getSuspensionData().data).containsOnly(breakpoint);
    }

    @Test
    public void evaluateConditionResponseIsReturned_whenThereIsABreakpointWithConditionWithHitCountFulfilled() {
        final RobotLineBreakpoint breakpoint = mock(RobotLineBreakpoint.class);
        when(breakpoint.evaluateHitCount()).thenReturn(true);
        when(breakpoint.isConditionEnabled()).thenReturn(true);
        when(breakpoint.getCondition()).thenReturn("Assert Equals    1    2");

        final Stacktrace stack = new Stacktrace();
        stack.push(new StackFrame("Suite", FrameCategory.SUITE, 0, context()));
        stack.push(new StackFrame("Test", FrameCategory.TEST, 1, context()));
        stack.push(new StackFrame("keyword", FrameCategory.KEYWORD, 2, breakpointContext(breakpoint)));

        final DebuggerPreferences prefs = new DebuggerPreferences(() -> false, true);
        final UserProcessDebugController controller = new UserProcessDebugController(stack, prefs);

        final Optional<ServerResponse> response = controller.takeCurrentResponse(PausingPoint.PRE_START_KEYWORD);

        assertThat(response).isNotEmpty().containsInstanceOf(EvaluateCondition.class);
        assertThat(response.get().toMessage()).isEqualTo("{\"evaluate_condition\":[\"Assert Equals\",\"1\",\"2\"]}");
        assertThat(controller.getSuspensionData().reason).isEqualByComparingTo(SuspendReason.BREAKPOINT);
        assertThat(controller.getSuspensionData().data).containsOnly(breakpoint);
    }

    @Test
    public void noResponseIsReturned_whenThereIsABreakpointButHitCountIsNotFulfilled() {
        final RobotLineBreakpoint breakpoint = mock(RobotLineBreakpoint.class);
        when(breakpoint.evaluateHitCount()).thenReturn(false);

        final Stacktrace stack = new Stacktrace();
        stack.push(new StackFrame("Suite", FrameCategory.SUITE, 0, context()));
        stack.push(new StackFrame("Test", FrameCategory.TEST, 1, context()));
        stack.push(new StackFrame("keyword", FrameCategory.KEYWORD, 2, breakpointContext(breakpoint)));

        final DebuggerPreferences prefs = new DebuggerPreferences(() -> false, true);
        final UserProcessDebugController controller = new UserProcessDebugController(stack, prefs);

        final Optional<ServerResponse> response = controller.takeCurrentResponse(PausingPoint.PRE_START_KEYWORD);

        assertThat(response).isEmpty();
        assertThat(controller.getSuspensionData()).isNull();
    }

    @Test
    public void noResponseIsReturned_whenThereIsNoBreakpoint() {
        final Stacktrace stack = new Stacktrace();
        stack.push(new StackFrame("Suite", FrameCategory.SUITE, 0, context()));
        stack.push(new StackFrame("Test", FrameCategory.TEST, 1, context()));
        stack.push(new StackFrame("keyword", FrameCategory.KEYWORD, 2, context()));

        final DebuggerPreferences prefs = new DebuggerPreferences(() -> false, true);
        final UserProcessDebugController controller = new UserProcessDebugController(stack, prefs);

        final Optional<ServerResponse> response = controller.takeCurrentResponse(PausingPoint.PRE_START_KEYWORD);

        assertThat(response).isEmpty();
        assertThat(controller.getSuspensionData()).isNull();
    }

    @Theory
    public void noResponseIsReturned_whenThereIsABreakpointButPausingPointIsOtherThanPreStart(
            final PausingPoint point) {
        assumeTrue(point != PausingPoint.PRE_START_KEYWORD);

        final RobotLineBreakpoint breakpoint = mock(RobotLineBreakpoint.class);
        when(breakpoint.evaluateHitCount()).thenReturn(true);
        when(breakpoint.isConditionEnabled()).thenReturn(false);

        final Stacktrace stack = new Stacktrace();
        stack.push(new StackFrame("Suite", FrameCategory.SUITE, 0, context()));
        stack.push(new StackFrame("Test", FrameCategory.TEST, 1, context()));
        stack.push(new StackFrame("keyword", FrameCategory.KEYWORD, 2, breakpointContext(breakpoint)));

        final DebuggerPreferences prefs = new DebuggerPreferences(() -> false, true);
        final UserProcessDebugController controller = new UserProcessDebugController(stack, prefs);

        final Optional<ServerResponse> response = controller.takeCurrentResponse(point);

        assertThat(response).isEmpty();
        assertThat(controller.getSuspensionData()).isNull();
    }

    @Theory
    public void pauseResponseIsReturned_whenSteppingIntoAndNotOmittingLibKeywords(final PausingPoint pausingPoint) {
        assumeTrue(pausingPoint != PausingPoint.END_KEYWORD);

        final Runnable whenSent = mock(Runnable.class);

        final Stacktrace stack = new Stacktrace();
        stack.push(new StackFrame("Suite", FrameCategory.SUITE, 0, context()));
        stack.push(new StackFrame("Test", FrameCategory.TEST, 1, context()));
        stack.push(new StackFrame("keyword", FrameCategory.KEYWORD, 2, context()));

        final DebuggerPreferences prefs = new DebuggerPreferences(() -> false, true);
        final UserProcessDebugController controller = new UserProcessDebugController(stack, prefs);
        controller.setSuspensionData(new SuspensionData(SuspendReason.STEPPING, SteppingMode.INTO, whenSent));

        final Optional<ServerResponse> response = controller.takeCurrentResponse(pausingPoint);
        assertThat(response).isPresent().containsInstanceOf(PauseExecution.class);
        assertThat(controller.getSuspensionData().reason).isEqualByComparingTo(SuspendReason.STEPPING);
        assertThat(controller.getSuspensionData().data).containsOnly(SteppingMode.INTO, whenSent);
        verify(whenSent).run();
    }

    @Theory
    public void pauseResponseIsReturned_whenSteppingIntoAndOmittingLibKeywords(final PausingPoint pausingPoint) {
        assumeTrue(pausingPoint != PausingPoint.END_KEYWORD && pausingPoint != PausingPoint.PRE_END_KEYWORD);

        final Runnable whenSent = mock(Runnable.class);

        final Stacktrace stack = new Stacktrace();
        stack.push(new StackFrame("Suite", FrameCategory.SUITE, 0, context()));
        stack.push(new StackFrame("Test", FrameCategory.TEST, 1, context()));
        stack.push(new StackFrame("keyword", FrameCategory.KEYWORD, 2, context()));

        final DebuggerPreferences prefs = new DebuggerPreferences(() -> false, false);
        final UserProcessDebugController controller = new UserProcessDebugController(stack, prefs);
        controller.setSuspensionData(new SuspensionData(SuspendReason.STEPPING, SteppingMode.INTO, whenSent));

        final Optional<ServerResponse> response = controller.takeCurrentResponse(pausingPoint);
        assertThat(response).isPresent().containsInstanceOf(PauseExecution.class);
        assertThat(controller.getSuspensionData().reason).isEqualByComparingTo(SuspendReason.STEPPING);
        assertThat(controller.getSuspensionData().data).containsOnly(SteppingMode.INTO, whenSent);
        verify(whenSent).run();
    }

    @Test
    public void noResponseIsReturned_whenSteppingIntoButPausingPointIsAtEndKeyword() {
        final Runnable whenSent = mock(Runnable.class);

        final Stacktrace stack = new Stacktrace();
        stack.push(new StackFrame("Suite", FrameCategory.SUITE, 0, context()));
        stack.push(new StackFrame("Test", FrameCategory.TEST, 1, context()));
        stack.push(new StackFrame("keyword", FrameCategory.KEYWORD, 2, context()));

        final DebuggerPreferences prefs = new DebuggerPreferences(() -> false, true);
        final UserProcessDebugController controller = new UserProcessDebugController(stack, prefs);
        controller.setSuspensionData(new SuspensionData(SuspendReason.STEPPING, SteppingMode.INTO, whenSent));

        final Optional<ServerResponse> response = controller.takeCurrentResponse(PausingPoint.END_KEYWORD);
        assertThat(response).isEmpty();
        assertThat(controller.getSuspensionData().reason).isEqualByComparingTo(SuspendReason.STEPPING);
        assertThat(controller.getSuspensionData().data).containsOnly(SteppingMode.INTO, whenSent);
        verifyZeroInteractions(whenSent);
    }

    @Theory
    public void noResponseIsReturned_whenSteppingIntoOmittingLibKeywordsButPausingPointIsAtEndKeyword(
            final PausingPoint pausingPoint) {
        assumeTrue(pausingPoint == PausingPoint.PRE_END_KEYWORD || pausingPoint == PausingPoint.END_KEYWORD);

        final Runnable whenSent = mock(Runnable.class);

        final Stacktrace stack = new Stacktrace();
        stack.push(new StackFrame("Suite", FrameCategory.SUITE, 0, context()));
        stack.push(new StackFrame("Test", FrameCategory.TEST, 1, context()));
        stack.push(new StackFrame("keyword", FrameCategory.KEYWORD, 2, context()));

        final DebuggerPreferences prefs = new DebuggerPreferences(() -> false, false);
        final UserProcessDebugController controller = new UserProcessDebugController(stack, prefs);
        controller.setSuspensionData(new SuspensionData(SuspendReason.STEPPING, SteppingMode.INTO, whenSent));

        final Optional<ServerResponse> response = controller.takeCurrentResponse(pausingPoint);
        assertThat(response).isEmpty();
        assertThat(controller.getSuspensionData().reason).isEqualByComparingTo(SuspendReason.STEPPING);
        assertThat(controller.getSuspensionData().data).containsOnly(SteppingMode.INTO, whenSent);
        verifyZeroInteractions(whenSent);
    }

    @Test
    public void noResponseIsReturned_whenSteppingIntoOmittingLibKeywordsButPausingPointIsAtEndKeyword() {
        final Runnable whenSent = mock(Runnable.class);

        final Stacktrace stack = new Stacktrace();
        stack.push(new StackFrame("Suite", FrameCategory.SUITE, 0, context()));
        stack.push(new StackFrame("Test", FrameCategory.TEST, 1, context()));
        stack.push(new StackFrame("keyword", FrameCategory.KEYWORD, 2, libKeywordContext()));

        final DebuggerPreferences prefs = new DebuggerPreferences(() -> false, false);
        final UserProcessDebugController controller = new UserProcessDebugController(stack, prefs);
        controller.setSuspensionData(new SuspensionData(SuspendReason.STEPPING, SteppingMode.INTO, whenSent));

        final Optional<ServerResponse> response = controller.takeCurrentResponse(PausingPoint.START_KEYWORD);
        assertThat(response).isEmpty();
        assertThat(controller.getSuspensionData().reason).isEqualByComparingTo(SuspendReason.STEPPING);
        assertThat(controller.getSuspensionData().data).containsOnly(SteppingMode.INTO, whenSent);
        verifyZeroInteractions(whenSent);
    }

    @Test
    public void noResponseIsReturned_whenSteppingIntoButForFrameIsOnTopOfTheStack() {
        final Runnable whenSent = mock(Runnable.class);

        final Stacktrace stack = new Stacktrace();
        stack.push(new StackFrame("Suite", FrameCategory.SUITE, 0, context()));
        stack.push(new StackFrame("Test", FrameCategory.TEST, 1, context()));
        stack.push(new StackFrame("keyword", FrameCategory.KEYWORD, 2, context()));
        stack.push(new StackFrame(":FOR", FrameCategory.FOR, 2, context()));

        final DebuggerPreferences prefs = new DebuggerPreferences(() -> false, true);
        final UserProcessDebugController controller = new UserProcessDebugController(stack, prefs);
        controller.setSuspensionData(new SuspensionData(SuspendReason.STEPPING, SteppingMode.INTO, whenSent));

        final Optional<ServerResponse> response = controller.takeCurrentResponse(PausingPoint.PRE_START_KEYWORD);
        assertThat(response).isEmpty();
        assertThat(controller.getSuspensionData().reason).isEqualByComparingTo(SuspendReason.STEPPING);
        assertThat(controller.getSuspensionData().data).containsOnly(SteppingMode.INTO, whenSent);
        verifyZeroInteractions(whenSent);
    }

    @Test
    public void pauseResponseIsReturned_whenSteppingOverOnPreStartKeyword_andTopFrameIsMarkedStepping() {
        final Runnable whenSent = mock(Runnable.class);

        final Stacktrace stack = new Stacktrace();
        stack.push(new StackFrame("Suite", FrameCategory.SUITE, 0, context()));
        stack.push(new StackFrame("Test", FrameCategory.TEST, 1, context()));
        stack.push(new StackFrame("keyword", FrameCategory.KEYWORD, 2, context()));
        stack.peekCurrentFrame().get().mark(StackFrameMarker.STEPPING);

        final DebuggerPreferences prefs = new DebuggerPreferences(() -> false, true);
        final UserProcessDebugController controller = new UserProcessDebugController(stack, prefs);
        controller.setSuspensionData(new SuspensionData(SuspendReason.STEPPING, SteppingMode.OVER, whenSent));

        final Optional<ServerResponse> response = controller.takeCurrentResponse(PausingPoint.PRE_START_KEYWORD);
        assertThat(response).isPresent().containsInstanceOf(PauseExecution.class);
        assertThat(controller.getSuspensionData().reason).isEqualByComparingTo(SuspendReason.STEPPING);
        assertThat(controller.getSuspensionData().data).containsOnly(SteppingMode.OVER, whenSent);
        verify(whenSent).run();
    }

    @Test
    public void pauseResponseIsReturned_whenSteppingOverOnPreStartKeyword_andThereIsNoFrameMarkedStepping() {
        final Runnable whenSent = mock(Runnable.class);

        final Stacktrace stack = new Stacktrace();
        stack.push(new StackFrame("Suite", FrameCategory.SUITE, 0, context()));
        stack.push(new StackFrame("Test", FrameCategory.TEST, 1, context()));
        stack.push(new StackFrame("keyword", FrameCategory.KEYWORD, 2, context()));

        final DebuggerPreferences prefs = new DebuggerPreferences(() -> false, true);
        final UserProcessDebugController controller = new UserProcessDebugController(stack, prefs);
        controller.setSuspensionData(new SuspensionData(SuspendReason.STEPPING, SteppingMode.OVER, whenSent));

        final Optional<ServerResponse> response = controller.takeCurrentResponse(PausingPoint.PRE_START_KEYWORD);
        assertThat(response).isPresent().containsInstanceOf(PauseExecution.class);
        assertThat(controller.getSuspensionData().reason).isEqualByComparingTo(SuspendReason.STEPPING);
        assertThat(controller.getSuspensionData().data).containsOnly(SteppingMode.OVER, whenSent);
        verify(whenSent).run();
    }

    @Test
    public void noResponseIsReturned_whenSteppingOverOnPreStartKeyword_andSomeInnerFrameIsMarkedStepping() {
        final Runnable whenSent = mock(Runnable.class);

        final Stacktrace stack = new Stacktrace();
        stack.push(new StackFrame("Suite", FrameCategory.SUITE, 0, context()));
        stack.push(new StackFrame("Test", FrameCategory.TEST, 1, context()));
        stack.push(new StackFrame("keyword2", FrameCategory.KEYWORD, 2, context()));
        stack.peekCurrentFrame().get().mark(StackFrameMarker.STEPPING);
        stack.push(new StackFrame("keyword1", FrameCategory.KEYWORD, 3, context()));

        final DebuggerPreferences prefs = new DebuggerPreferences(() -> false, true);
        final UserProcessDebugController controller = new UserProcessDebugController(stack, prefs);
        controller.setSuspensionData(new SuspensionData(SuspendReason.STEPPING, SteppingMode.OVER, whenSent));

        final Optional<ServerResponse> response = controller.takeCurrentResponse(PausingPoint.PRE_START_KEYWORD);
        assertThat(response).isEmpty();
        assertThat(controller.getSuspensionData().reason).isEqualByComparingTo(SuspendReason.STEPPING);
        assertThat(controller.getSuspensionData().data).containsOnly(SteppingMode.OVER, whenSent);
        verifyZeroInteractions(whenSent);
    }

    @Test
    public void noResponseIsReturned_whenSteppingOverOnPreStartKeyword_andTopForFrameIsMarkedStepping() {
        final Runnable whenSent = mock(Runnable.class);

        final Stacktrace stack = new Stacktrace();
        stack.push(new StackFrame("Suite", FrameCategory.SUITE, 0, context()));
        stack.push(new StackFrame("Test", FrameCategory.TEST, 1, context()));
        stack.push(new StackFrame("keyword2", FrameCategory.KEYWORD, 2, context()));
        stack.push(new StackFrame(":FOR", FrameCategory.FOR, 2, context()));
        stack.peekCurrentFrame().get().mark(StackFrameMarker.STEPPING);

        final DebuggerPreferences prefs = new DebuggerPreferences(() -> false, true);
        final UserProcessDebugController controller = new UserProcessDebugController(stack, prefs);
        controller.setSuspensionData(new SuspensionData(SuspendReason.STEPPING, SteppingMode.OVER, whenSent));

        final Optional<ServerResponse> response = controller.takeCurrentResponse(PausingPoint.PRE_START_KEYWORD);
        assertThat(response).isEmpty();
        assertThat(controller.getSuspensionData().reason).isEqualByComparingTo(SuspendReason.STEPPING);
        assertThat(controller.getSuspensionData().data).containsOnly(SteppingMode.OVER, whenSent);
        verifyZeroInteractions(whenSent);
    }

    @Theory
    public void noResponseIsReturned_whenSteppingOverOnPreStartKeyword_andPausingPointIsOnEnd(
            final PausingPoint pausingPoint) {
        assumeTrue(pausingPoint == PausingPoint.PRE_END_KEYWORD || pausingPoint == PausingPoint.END_KEYWORD);

        final Runnable whenSent = mock(Runnable.class);

        final Stacktrace stack = new Stacktrace();
        stack.push(new StackFrame("Suite", FrameCategory.SUITE, 0, context()));
        stack.push(new StackFrame("Test", FrameCategory.TEST, 1, context()));
        stack.push(new StackFrame("keyword2", FrameCategory.KEYWORD, 2, context()));
        stack.peekCurrentFrame().get().mark(StackFrameMarker.STEPPING);

        final DebuggerPreferences prefs = new DebuggerPreferences(() -> false, true);
        final UserProcessDebugController controller = new UserProcessDebugController(stack, prefs);
        controller.setSuspensionData(new SuspensionData(SuspendReason.STEPPING, SteppingMode.OVER, whenSent));

        final Optional<ServerResponse> response = controller.takeCurrentResponse(pausingPoint);
        assertThat(response).isEmpty();
        assertThat(controller.getSuspensionData().reason).isEqualByComparingTo(SuspendReason.STEPPING);
        assertThat(controller.getSuspensionData().data).containsOnly(SteppingMode.OVER, whenSent);
        verifyZeroInteractions(whenSent);
    }

    @Test
    public void pauseResponseIsReturned_whenSteppingOverOnStartKeywordWithForItemFrame_andForFrameIsMarkedStepping() {
        final Runnable whenSent = mock(Runnable.class);

        final Stacktrace stack = new Stacktrace();
        stack.push(new StackFrame("Suite", FrameCategory.SUITE, 0, context()));
        stack.push(new StackFrame("Test", FrameCategory.TEST, 1, context()));
        stack.push(new StackFrame("keyword", FrameCategory.KEYWORD, 2, context()));
        stack.push(new StackFrame(":FOR", FrameCategory.FOR, 2, context()));
        stack.peekCurrentFrame().get().mark(StackFrameMarker.STEPPING);
        stack.push(new StackFrame("${i}=0", FrameCategory.FOR_ITEM, 2, context()));

        final DebuggerPreferences prefs = new DebuggerPreferences(() -> false, true);
        final UserProcessDebugController controller = new UserProcessDebugController(stack, prefs);
        controller.setSuspensionData(new SuspensionData(SuspendReason.STEPPING, SteppingMode.OVER, whenSent));

        final Optional<ServerResponse> response = controller.takeCurrentResponse(PausingPoint.START_KEYWORD);
        assertThat(response).isPresent().containsInstanceOf(PauseExecution.class);
        assertThat(controller.getSuspensionData().reason).isEqualByComparingTo(SuspendReason.STEPPING);
        assertThat(controller.getSuspensionData().data).containsOnly(SteppingMode.OVER, whenSent);
        verify(whenSent).run();
    }

    @Test
    public void pauseResponseIsReturned_whenSteppingOverOnStartKeywordWithForItemFrame_andThereIsNoFrameMarkedStepping() {
        final Runnable whenSent = mock(Runnable.class);

        final Stacktrace stack = new Stacktrace();
        stack.push(new StackFrame("Suite", FrameCategory.SUITE, 0, context()));
        stack.push(new StackFrame("Test", FrameCategory.TEST, 1, context()));
        stack.push(new StackFrame("keyword", FrameCategory.KEYWORD, 2, context()));
        stack.push(new StackFrame(":FOR", FrameCategory.FOR, 2, context()));
        stack.push(new StackFrame("${i}=0", FrameCategory.FOR_ITEM, 2, context()));

        final DebuggerPreferences prefs = new DebuggerPreferences(() -> false, true);
        final UserProcessDebugController controller = new UserProcessDebugController(stack, prefs);
        controller.setSuspensionData(new SuspensionData(SuspendReason.STEPPING, SteppingMode.OVER, whenSent));

        final Optional<ServerResponse> response = controller.takeCurrentResponse(PausingPoint.START_KEYWORD);
        assertThat(response).isPresent().containsInstanceOf(PauseExecution.class);
        assertThat(controller.getSuspensionData().reason).isEqualByComparingTo(SuspendReason.STEPPING);
        assertThat(controller.getSuspensionData().data).containsOnly(SteppingMode.OVER, whenSent);
        verify(whenSent).run();
    }

    @Test
    public void noResponseIsReturned_whenSteppingOverOnStartKeywordWithForItemFrame_andSomeInnerFrameIsMarkedStepping() {
        final Runnable whenSent = mock(Runnable.class);

        final Stacktrace stack = new Stacktrace();
        stack.push(new StackFrame("Suite", FrameCategory.SUITE, 0, context()));
        stack.push(new StackFrame("Test", FrameCategory.TEST, 1, context()));
        stack.push(new StackFrame("keyword", FrameCategory.KEYWORD, 2, context()));
        stack.peekCurrentFrame().get().mark(StackFrameMarker.STEPPING);
        stack.push(new StackFrame(":FOR", FrameCategory.FOR, 2, context()));
        stack.push(new StackFrame("${i}=0", FrameCategory.FOR_ITEM, 2, context()));

        final DebuggerPreferences prefs = new DebuggerPreferences(() -> false, true);
        final UserProcessDebugController controller = new UserProcessDebugController(stack, prefs);
        controller.setSuspensionData(new SuspensionData(SuspendReason.STEPPING, SteppingMode.OVER, whenSent));

        final Optional<ServerResponse> response = controller.takeCurrentResponse(PausingPoint.START_KEYWORD);
        assertThat(response).isEmpty();
        assertThat(controller.getSuspensionData().reason).isEqualByComparingTo(SuspendReason.STEPPING);
        assertThat(controller.getSuspensionData().data).containsOnly(SteppingMode.OVER, whenSent);
        verifyZeroInteractions(whenSent);
    }

    @Test
    public void noResponseIsReturned_whenSteppingOverOnStartKeywordWithKeywordFrame() {
        final Runnable whenSent = mock(Runnable.class);

        final Stacktrace stack = new Stacktrace();
        stack.push(new StackFrame("Suite", FrameCategory.SUITE, 0, context()));
        stack.push(new StackFrame("Test", FrameCategory.TEST, 1, context()));
        stack.push(new StackFrame("keyword", FrameCategory.KEYWORD, 2, context()));
        stack.peekCurrentFrame().get().mark(StackFrameMarker.STEPPING);
        stack.push(new StackFrame("keyword2", FrameCategory.KEYWORD, 3, context()));

        final DebuggerPreferences prefs = new DebuggerPreferences(() -> false, true);
        final UserProcessDebugController controller = new UserProcessDebugController(stack, prefs);
        controller.setSuspensionData(new SuspensionData(SuspendReason.STEPPING, SteppingMode.OVER, whenSent));

        final Optional<ServerResponse> response = controller.takeCurrentResponse(PausingPoint.START_KEYWORD);
        assertThat(response).isEmpty();
        assertThat(controller.getSuspensionData().reason).isEqualByComparingTo(SuspendReason.STEPPING);
        assertThat(controller.getSuspensionData().data).containsOnly(SteppingMode.OVER, whenSent);
        verifyZeroInteractions(whenSent);
    }

    @Theory
    public void pauseResponseIsReturned_whenSteppingReturnAndThereIsNoMarkedFrameOnStack_steppingIntoLibEnabled(
            final PausingPoint pausingPoint) {
        assumeTrue(pausingPoint == PausingPoint.PRE_START_KEYWORD || pausingPoint == PausingPoint.PRE_END_KEYWORD);

        final Runnable whenSent = mock(Runnable.class);

        final Stacktrace stack = new Stacktrace();
        stack.push(new StackFrame("Suite", FrameCategory.SUITE, 0, context()));
        stack.push(new StackFrame("Test", FrameCategory.TEST, 1, context()));
        stack.push(new StackFrame("keyword", FrameCategory.KEYWORD, 2, context()));

        final DebuggerPreferences prefs = new DebuggerPreferences(() -> false, true);
        final UserProcessDebugController controller = new UserProcessDebugController(stack, prefs);
        controller.setSuspensionData(new SuspensionData(SuspendReason.STEPPING, SteppingMode.RETURN, whenSent));

        final Optional<ServerResponse> response = controller.takeCurrentResponse(pausingPoint);
        assertThat(response).isPresent().containsInstanceOf(PauseExecution.class);
        assertThat(controller.getSuspensionData().reason).isEqualByComparingTo(SuspendReason.STEPPING);
        assertThat(controller.getSuspensionData().data).containsOnly(SteppingMode.RETURN, whenSent);
        verify(whenSent).run();
    }

    @Theory
    public void pauseResponseIsReturned_whenSteppingReturnAndThereIsNoMarkedFrameOnStack_steppingIntoLibDisabled(
            final PausingPoint pausingPoint) {
        assumeTrue(pausingPoint == PausingPoint.PRE_START_KEYWORD || pausingPoint == PausingPoint.PRE_END_KEYWORD);

        final Runnable whenSent = mock(Runnable.class);

        final Stacktrace stack = new Stacktrace();
        stack.push(new StackFrame("Suite", FrameCategory.SUITE, 0, context()));
        stack.push(new StackFrame("Test", FrameCategory.TEST, 1, context()));
        stack.push(new StackFrame("keyword", FrameCategory.KEYWORD, 2, context()));

        final DebuggerPreferences prefs = new DebuggerPreferences(() -> false, false);
        final UserProcessDebugController controller = new UserProcessDebugController(stack, prefs);
        controller.setSuspensionData(new SuspensionData(SuspendReason.STEPPING, SteppingMode.RETURN, whenSent));

        final Optional<ServerResponse> response = controller.takeCurrentResponse(pausingPoint);
        assertThat(response).isPresent().containsInstanceOf(PauseExecution.class);
        assertThat(controller.getSuspensionData().reason).isEqualByComparingTo(SuspendReason.STEPPING);
        assertThat(controller.getSuspensionData().data).containsOnly(SteppingMode.RETURN, whenSent);
        verify(whenSent).run();
    }

    @Theory
    public void noResponseIsReturned_whenSteppingReturnAndThereIsMarkedFrameOnStack_steppingIntoLibEnabled(
            final PausingPoint pausingPoint) {
        assumeTrue(pausingPoint == PausingPoint.PRE_START_KEYWORD || pausingPoint == PausingPoint.PRE_END_KEYWORD);

        final Runnable whenSent = mock(Runnable.class);

        final Stacktrace stack = new Stacktrace();
        stack.push(new StackFrame("Suite", FrameCategory.SUITE, 0, context()));
        stack.push(new StackFrame("Test", FrameCategory.TEST, 1, context()));
        stack.peekCurrentFrame().get().mark(StackFrameMarker.STEPPING);
        stack.push(new StackFrame("keyword", FrameCategory.KEYWORD, 2, context()));

        final DebuggerPreferences prefs = new DebuggerPreferences(() -> false, true);
        final UserProcessDebugController controller = new UserProcessDebugController(stack, prefs);
        controller.setSuspensionData(new SuspensionData(SuspendReason.STEPPING, SteppingMode.RETURN, whenSent));

        final Optional<ServerResponse> response = controller.takeCurrentResponse(pausingPoint);
        assertThat(response).isEmpty();
        assertThat(controller.getSuspensionData().reason).isEqualByComparingTo(SuspendReason.STEPPING);
        assertThat(controller.getSuspensionData().data).containsOnly(SteppingMode.RETURN, whenSent);
        verifyZeroInteractions(whenSent);
    }

    @Theory
    public void noResponseIsReturned_whenSteppingReturnAndThereIsMarkedFrameOnStack_steppingIntoLibDisabled(
            final PausingPoint pausingPoint) {
        assumeTrue(pausingPoint == PausingPoint.PRE_START_KEYWORD || pausingPoint == PausingPoint.PRE_END_KEYWORD);

        final Runnable whenSent = mock(Runnable.class);

        final Stacktrace stack = new Stacktrace();
        stack.push(new StackFrame("Suite", FrameCategory.SUITE, 0, context()));
        stack.push(new StackFrame("Test", FrameCategory.TEST, 1, context()));
        stack.push(new StackFrame("keyword", FrameCategory.KEYWORD, 2, context()));
        stack.peekCurrentFrame().get().mark(StackFrameMarker.STEPPING);

        final DebuggerPreferences prefs = new DebuggerPreferences(() -> false, false);
        final UserProcessDebugController controller = new UserProcessDebugController(stack, prefs);
        controller.setSuspensionData(new SuspensionData(SuspendReason.STEPPING, SteppingMode.RETURN, whenSent));

        final Optional<ServerResponse> response = controller.takeCurrentResponse(pausingPoint);
        assertThat(response).isEmpty();
        assertThat(controller.getSuspensionData().reason).isEqualByComparingTo(SuspendReason.STEPPING);
        assertThat(controller.getSuspensionData().data).containsOnly(SteppingMode.RETURN, whenSent);
        verifyZeroInteractions(whenSent);
    }

    @Theory
    public void noResponseIsReturned_whenSteppingReturnAtStartOrEndPausingPoint_steppingIntoLibEnabled(
            final PausingPoint pausingPoint) {
        assumeTrue(pausingPoint == PausingPoint.START_KEYWORD || pausingPoint == PausingPoint.END_KEYWORD);

        final Runnable whenSent = mock(Runnable.class);

        final Stacktrace stack = new Stacktrace();
        stack.push(new StackFrame("Suite", FrameCategory.SUITE, 0, context()));
        stack.push(new StackFrame("Test", FrameCategory.TEST, 1, context()));
        stack.push(new StackFrame("keyword", FrameCategory.KEYWORD, 2, context()));

        final DebuggerPreferences prefs = new DebuggerPreferences(() -> false, true);
        final UserProcessDebugController controller = new UserProcessDebugController(stack, prefs);
        controller.setSuspensionData(new SuspensionData(SuspendReason.STEPPING, SteppingMode.RETURN, whenSent));

        final Optional<ServerResponse> response = controller.takeCurrentResponse(pausingPoint);
        assertThat(response).isEmpty();
        assertThat(controller.getSuspensionData().reason).isEqualByComparingTo(SuspendReason.STEPPING);
        assertThat(controller.getSuspensionData().data).containsOnly(SteppingMode.RETURN, whenSent);
        verifyZeroInteractions(whenSent);
    }

    @Theory
    public void noResponseIsReturned_whenSteppingReturnAtStartOrEndPausingPoint_steppingIntoLibDisabled(
            final PausingPoint pausingPoint) {
        assumeTrue(pausingPoint == PausingPoint.START_KEYWORD || pausingPoint == PausingPoint.END_KEYWORD);

        final Runnable whenSent = mock(Runnable.class);

        final Stacktrace stack = new Stacktrace();
        stack.push(new StackFrame("Suite", FrameCategory.SUITE, 0, context()));
        stack.push(new StackFrame("Test", FrameCategory.TEST, 1, context()));
        stack.push(new StackFrame("keyword", FrameCategory.KEYWORD, 2, context()));

        final DebuggerPreferences prefs = new DebuggerPreferences(() -> false, false);
        final UserProcessDebugController controller = new UserProcessDebugController(stack, prefs);
        controller.setSuspensionData(new SuspensionData(SuspendReason.STEPPING, SteppingMode.RETURN, whenSent));

        final Optional<ServerResponse> response = controller.takeCurrentResponse(pausingPoint);
        assertThat(response).isEmpty();
        assertThat(controller.getSuspensionData().reason).isEqualByComparingTo(SuspendReason.STEPPING);
        assertThat(controller.getSuspensionData().data).containsOnly(SteppingMode.RETURN, whenSent);
        verifyZeroInteractions(whenSent);
    }

    @Theory
    public void pauseResponseIsReturned_whenSteppingReturnAndThereIsLibraryKeywordFrameOnStack_steppingIntoLibEnabled(
            final PausingPoint pausingPoint) {
        assumeTrue(pausingPoint == PausingPoint.PRE_START_KEYWORD || pausingPoint == PausingPoint.PRE_END_KEYWORD);

        final Runnable whenSent = mock(Runnable.class);

        final Stacktrace stack = new Stacktrace();
        stack.push(new StackFrame("Suite", FrameCategory.SUITE, 0, context()));
        stack.push(new StackFrame("Test", FrameCategory.TEST, 1, context()));
        stack.push(new StackFrame("keyword", FrameCategory.KEYWORD, 2, libKeywordContext()));

        final DebuggerPreferences prefs = new DebuggerPreferences(() -> false, true);
        final UserProcessDebugController controller = new UserProcessDebugController(stack, prefs);
        controller.setSuspensionData(new SuspensionData(SuspendReason.STEPPING, SteppingMode.RETURN, whenSent));

        final Optional<ServerResponse> response = controller.takeCurrentResponse(pausingPoint);
        assertThat(response).isPresent().containsInstanceOf(PauseExecution.class);
        assertThat(controller.getSuspensionData().reason).isEqualByComparingTo(SuspendReason.STEPPING);
        assertThat(controller.getSuspensionData().data).containsOnly(SteppingMode.RETURN, whenSent);
        verify(whenSent).run();
    }

    @Theory
    public void noResponseIsReturned_whenSteppingReturnAndThereIsLibraryKeywordFrameOnStack_steppingIntoLibDisabled(
            final PausingPoint pausingPoint) {
        assumeTrue(pausingPoint == PausingPoint.PRE_START_KEYWORD || pausingPoint == PausingPoint.PRE_END_KEYWORD);

        final Runnable whenSent = mock(Runnable.class);

        final Stacktrace stack = new Stacktrace();
        stack.push(new StackFrame("Suite", FrameCategory.SUITE, 0, context()));
        stack.push(new StackFrame("Test", FrameCategory.TEST, 1, context()));
        stack.push(new StackFrame("keyword", FrameCategory.KEYWORD, 2, libKeywordContext()));

        final DebuggerPreferences prefs = new DebuggerPreferences(() -> false, false);
        final UserProcessDebugController controller = new UserProcessDebugController(stack, prefs);
        controller.setSuspensionData(new SuspensionData(SuspendReason.STEPPING, SteppingMode.RETURN, whenSent));

        final Optional<ServerResponse> response = controller.takeCurrentResponse(pausingPoint);
        assertThat(response).isEmpty();
        assertThat(controller.getSuspensionData().reason).isEqualByComparingTo(SuspendReason.STEPPING);
        assertThat(controller.getSuspensionData().data).containsOnly(SteppingMode.RETURN, whenSent);
        verifyZeroInteractions(whenSent);
    }

    @Test
    public void whenSteppingIntoIsRequested_resumeResponseIsQueuedAndStateOfControllerChangesToStepping() {
        final Stacktrace stack = new Stacktrace();
        final DebuggerPreferences prefs = new DebuggerPreferences(() -> false, true);
        final UserProcessDebugController controller = new UserProcessDebugController(stack, prefs);
        final Runnable callbackWhenSent = mock(Runnable.class);
        final Runnable callbackForStepEnd = mock(Runnable.class);

        controller.stepInto(callbackWhenSent, callbackForStepEnd);

        assertThat(controller.manualUserResponse).hasSize(1);
        final Optional<ServerResponse> response = controller.takeCurrentResponse(PausingPoint.START_KEYWORD);

        assertThat(response).containsInstanceOf(ResumeExecution.class);
        assertThat(controller.getSuspensionData().reason).isEqualTo(SuspendReason.STEPPING);
        assertThat(controller.getSuspensionData().data).containsExactly(SteppingMode.INTO, callbackForStepEnd);
        assertThat(controller.manualUserResponse).isEmpty();
        verify(callbackWhenSent).run();
    }

    @Test
    public void whenSteppingOverIsRequestedAndLastPointWasAtStartKeyword_resumeResponseIsQueuedAndStateOfControllerChangesToStepping() {
        final Stacktrace stack = new Stacktrace();
        stack.push(new StackFrame("Suite", FrameCategory.SUITE, 0, context()));
        stack.push(new StackFrame("Test", FrameCategory.TEST, 1, context()));
        stack.push(new StackFrame("keyword", FrameCategory.KEYWORD, 2, context()));

        final DebuggerPreferences prefs = new DebuggerPreferences(() -> false, true);
        final UserProcessDebugController controller = new UserProcessDebugController(stack, prefs);
        controller.setLastPausingPoint(PausingPoint.START_KEYWORD);
        final Runnable callbackWhenSent = mock(Runnable.class);
        final Runnable callbackForStepEnd = mock(Runnable.class);

        controller.stepOver(stack.peekCurrentFrame().get(), callbackWhenSent, callbackForStepEnd);

        assertThat(stack.stream().filter(StackFrame::isMarkedStepping)).hasSize(1)
                .containsExactly(stack.getFirstFrameSatisfying(f -> f.hasCategory(FrameCategory.TEST)).get());
        assertThat(controller.manualUserResponse).hasSize(1);
        final Optional<ServerResponse> response = controller.takeCurrentResponse(PausingPoint.START_KEYWORD);

        assertThat(response).containsInstanceOf(ResumeExecution.class);
        assertThat(controller.getSuspensionData().reason).isEqualTo(SuspendReason.STEPPING);
        assertThat(controller.getSuspensionData().data).containsExactly(SteppingMode.OVER, callbackForStepEnd);
        assertThat(controller.manualUserResponse).isEmpty();
        verify(callbackWhenSent).run();
    }

    @Theory
    public void whenSteppingOverIsRequestedAndLastPointWasDifferentThanStartKeyword_resumeResponseIsQueuedAndStateOfControllerChangesToStepping(
            final PausingPoint pausingPoint) {
        assumeTrue(pausingPoint != PausingPoint.START_KEYWORD);

        final Stacktrace stack = new Stacktrace();
        stack.push(new StackFrame("Suite", FrameCategory.SUITE, 0, context()));
        stack.push(new StackFrame("Test", FrameCategory.TEST, 1, context()));
        stack.push(new StackFrame("keyword", FrameCategory.KEYWORD, 2, context()));

        final DebuggerPreferences prefs = new DebuggerPreferences(() -> false, true);
        final UserProcessDebugController controller = new UserProcessDebugController(stack, prefs);
        controller.setLastPausingPoint(pausingPoint);
        final Runnable callbackWhenSent = mock(Runnable.class);
        final Runnable callbackForStepEnd = mock(Runnable.class);

        controller.stepOver(stack.peekCurrentFrame().get(), callbackWhenSent, callbackForStepEnd);

        assertThat(stack.stream().filter(StackFrame::isMarkedStepping)).hasSize(1)
                .containsExactly(stack.peekCurrentFrame().get());
        assertThat(controller.manualUserResponse).hasSize(1);
        final Optional<ServerResponse> response = controller.takeCurrentResponse(PausingPoint.START_KEYWORD);

        assertThat(response).containsInstanceOf(ResumeExecution.class);
        assertThat(controller.getSuspensionData().reason).isEqualTo(SuspendReason.STEPPING);
        assertThat(controller.getSuspensionData().data).containsExactly(SteppingMode.OVER, callbackForStepEnd);
        assertThat(controller.manualUserResponse).isEmpty();
        verify(callbackWhenSent).run();
    }

    @Test
    public void whenSteppingReturnIsRequested_resumeResponseIsQueuedAndStateOfControllerChangesToStepping() {
        final Stacktrace stack = new Stacktrace();
        stack.push(new StackFrame("Suite", FrameCategory.SUITE, 0, context()));
        stack.push(new StackFrame("Test", FrameCategory.TEST, 1, context()));
        stack.push(new StackFrame("keyword", FrameCategory.KEYWORD, 2, context()));

        final DebuggerPreferences prefs = new DebuggerPreferences(() -> false, true);
        final UserProcessDebugController controller = new UserProcessDebugController(stack, prefs);
        final Runnable callbackWhenSent = mock(Runnable.class);
        final Runnable callbackForStepEnd = mock(Runnable.class);

        controller.stepReturn(stack.peekCurrentFrame().get(), callbackWhenSent, callbackForStepEnd);

        assertThat(stack.stream().filter(StackFrame::isMarkedStepping)).hasSize(1)
                .containsExactly(stack.peekCurrentFrame().get());
        assertThat(controller.manualUserResponse).hasSize(1);
        final Optional<ServerResponse> response = controller.takeCurrentResponse(PausingPoint.START_KEYWORD);

        assertThat(response).containsInstanceOf(ResumeExecution.class);
        assertThat(controller.getSuspensionData().reason).isEqualTo(SuspendReason.STEPPING);
        assertThat(controller.getSuspensionData().data).containsExactly(SteppingMode.RETURN, callbackForStepEnd);
        assertThat(controller.manualUserResponse).isEmpty();
        verify(callbackWhenSent).run();
    }

    @Test
    public void whenVariableChangeIsRequested_properResponseIsQueued() {
        final Stacktrace stack = new Stacktrace();
        stack.push(new StackFrame("Suite", FrameCategory.SUITE, 0, context()));
        stack.push(new StackFrame("Test", FrameCategory.TEST, 1, context()));
        stack.push(new StackFrame("keyword", FrameCategory.KEYWORD, 2, context()));

        final DebuggerPreferences prefs = new DebuggerPreferences(() -> false, true);
        final UserProcessDebugController controller = new UserProcessDebugController(stack, prefs);

        final StackFrameVariable variable = new StackFrameVariable(VariableScope.TEST_SUITE, true, "var", "int", 42);
        controller.changeVariable(stack.peekCurrentFrame().get(), variable, newArrayList("1", "2"));

        assertThat(controller.manualUserResponse).hasSize(1);
        final Optional<ServerResponse> response = controller.takeCurrentResponse(PausingPoint.START_KEYWORD);

        assertThat(controller.manualUserResponse).isEmpty();
        assertThat(response).containsInstanceOf(ChangeVariable.class);
        assertThat(controller.getSuspensionData().reason).isEqualTo(SuspendReason.VARIABLE_CHANGE);
        assertThat(controller.getSuspensionData().data).containsExactly(2);
    }

    @Test
    public void whenVariableInnerValueChangeIsRequested_properResponseIsQueued() {
        final Stacktrace stack = new Stacktrace();
        stack.push(new StackFrame("Suite", FrameCategory.SUITE, 0, context()));
        stack.push(new StackFrame("Test", FrameCategory.TEST, 1, context()));
        stack.push(new StackFrame("keyword", FrameCategory.KEYWORD, 2, context()));

        final DebuggerPreferences prefs = new DebuggerPreferences(() -> false, true);
        final UserProcessDebugController controller = new UserProcessDebugController(stack, prefs);

        final StackFrameVariable variable = new StackFrameVariable(VariableScope.TEST_SUITE, true, "var", "int", 42);
        controller.changeVariableInnerValue(stack.peekCurrentFrame().get(), variable, newArrayList("0", "key"),
                newArrayList("1", "2"));

        assertThat(controller.manualUserResponse).hasSize(1);
        final Optional<ServerResponse> response = controller.takeCurrentResponse(PausingPoint.START_KEYWORD);

        assertThat(controller.manualUserResponse).isEmpty();
        assertThat(response).containsInstanceOf(ChangeVariable.class);
        assertThat(controller.getSuspensionData().reason).isEqualTo(SuspendReason.VARIABLE_CHANGE);
        assertThat(controller.getSuspensionData().data).containsExactly(2);
    }

    @Test
    public void whenFutureResponseWasOrdered_itIsReturnedAsFutureTask() throws Exception {
        final Stacktrace stack = new Stacktrace();
        final DebuggerPreferences prefs = new DebuggerPreferences(() -> false, true);
        final UserProcessDebugController controller = new UserProcessDebugController(stack, prefs);

        final ServerResponse response = () -> "response";
        final Runnable callback = mock(Runnable.class);
        controller.manualUserResponse.put(new ResponseWithCallback(response, callback));

        final FutureTask<ServerResponse> futureResponse = controller.takeFutureResponse();

        assertThat(controller.manualUserResponse).hasSize(1);

        futureResponse.run();
        assertThat(controller.manualUserResponse).isEmpty();
        verify(callback).run();
    }

    private static StackFrameContext context() {
        return mock(StackFrameContext.class);
    }

    private static StackFrameContext breakpointContext(final RobotLineBreakpoint breakpoint) {
        final StackFrameContext context = mock(StackFrameContext.class);
        when(context.getLineBreakpoint()).thenReturn(Optional.of(breakpoint));
        return context;
    }

    private static StackFrameContext erroneousContext(final String errorMsg) {
        final StackFrameContext context = mock(StackFrameContext.class);
        when(context.isErroneous()).thenReturn(true);
        when(context.getErrorMessage()).thenReturn(Optional.of(errorMsg));
        return context;
    }

    private StackFrameContext libKeywordContext() {
        final KeywordContext context = mock(KeywordContext.class);
        when(context.isLibraryKeywordContext()).thenReturn(true);
        return context;
    }
}
