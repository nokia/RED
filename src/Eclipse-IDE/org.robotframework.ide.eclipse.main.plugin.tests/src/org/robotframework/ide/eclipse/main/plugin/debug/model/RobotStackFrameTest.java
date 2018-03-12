/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.debug.model;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.core.commands.Command;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.jface.commands.PersistentState;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.rf.ide.core.execution.agent.event.Variable;
import org.rf.ide.core.execution.agent.event.VariableTypedValue;
import org.rf.ide.core.execution.debug.StackFrame;
import org.rf.ide.core.execution.debug.StackFrameVariable;
import org.rf.ide.core.execution.debug.StackFrameVariables;
import org.rf.ide.core.execution.debug.StackFrameVariables.StackVariablesDelta;
import org.rf.ide.core.execution.debug.Stacktrace;
import org.rf.ide.core.execution.debug.UserProcessDebugController;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.FileRegion;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableScope;
import org.robotframework.ide.eclipse.main.plugin.debug.AlwaysDisplaySortedVariablesHandler;
import org.robotframework.ide.eclipse.main.plugin.launch.IRobotProcess;

public class RobotStackFrameTest {

    @Test
    public void stackFramePropertiesTest() {
        final StackFrame frame = mock(StackFrame.class);
        when(frame.getName()).thenReturn("frame");
        when(frame.getCurrentSourcePath()).thenReturn(Optional.of(URI.create("file:///current_path.robot")));
        when(frame.getContextPath()).thenReturn(Optional.of(URI.create("file:///context_path.robot")));
        when(frame.getFileRegion()).thenReturn(
                Optional.of(new FileRegion(new FilePosition(1729, 0, 2000), new FilePosition(1729, 100, 2100))));

        final RobotThread thread = mock(RobotThread.class);
        final RobotStackFrame stackFrame = new RobotStackFrame(thread, frame, mock(UserProcessDebugController.class));

        assertThat(stackFrame.getFrame()).isSameAs(frame);
        assertThat(stackFrame.getThread()).isSameAs(thread);
        assertThat(stackFrame.getName()).isEqualTo("frame");
        assertThat(stackFrame.getPath()).contains(URI.create("file:///current_path.robot"));
        assertThat(stackFrame.getContextPath()).contains(URI.create("file:///context_path.robot"));
        assertThat(stackFrame.getLineNumber()).isEqualTo(1729);
        assertThat(stackFrame.getCharStart()).isEqualTo(2000);
        assertThat(stackFrame.getCharEnd()).isEqualTo(2100);
        assertThat(stackFrame.hasRegisterGroups()).isFalse();
        assertThat(stackFrame.getRegisterGroups()).isNull();
    }

    @Test
    public void erroneousFrameTest() {
        final StackFrame frame1 = mock(StackFrame.class);
        when(frame1.isErroneous()).thenReturn(true);
        final RobotStackFrame stackFrame1 = new RobotStackFrame(mock(RobotThread.class), frame1,
                mock(UserProcessDebugController.class));

        final StackFrame frame2 = mock(StackFrame.class);
        when(frame2.isErroneous()).thenReturn(false);
        final RobotStackFrame stackFrame2 = new RobotStackFrame(mock(RobotThread.class), frame2,
                mock(UserProcessDebugController.class));

        assertThat(stackFrame1.isErroneous()).isTrue();
        assertThat(stackFrame2.isErroneous()).isFalse();
    }

    @Test
    public void libraryKeywordFrameTest() {
        final StackFrame frame1 = mock(StackFrame.class);
        when(frame1.isLibraryKeywordFrame()).thenReturn(true);
        final RobotStackFrame stackFrame1 = new RobotStackFrame(mock(RobotThread.class), frame1,
                mock(UserProcessDebugController.class));

        final StackFrame frame2 = mock(StackFrame.class);
        when(frame2.isLibraryKeywordFrame()).thenReturn(false);
        final RobotStackFrame stackFrame2 = new RobotStackFrame(mock(RobotThread.class), frame2,
                mock(UserProcessDebugController.class));

        assertThat(stackFrame1.isLibraryKeywordFrame()).isTrue();
        assertThat(stackFrame2.isLibraryKeywordFrame()).isFalse();
    }

    @Test
    public void suiteDirectoryFrameTest() {
        final StackFrame frame1 = mock(StackFrame.class);
        when(frame1.isSuiteDirectoryContext()).thenReturn(true);
        final RobotStackFrame stackFrame1 = new RobotStackFrame(mock(RobotThread.class), frame1,
                mock(UserProcessDebugController.class));

        final StackFrame frame2 = mock(StackFrame.class);
        when(frame2.isSuiteDirectoryContext()).thenReturn(false);
        final RobotStackFrame stackFrame2 = new RobotStackFrame(mock(RobotThread.class), frame2,
                mock(UserProcessDebugController.class));

        assertThat(stackFrame1.isSuiteDirectoryContext()).isTrue();
        assertThat(stackFrame2.isSuiteDirectoryContext()).isFalse();
    }

    @Test
    public void suiteFileFrameTest() {
        final StackFrame frame1 = mock(StackFrame.class);
        when(frame1.isSuiteFileContext()).thenReturn(true);
        final RobotStackFrame stackFrame1 = new RobotStackFrame(mock(RobotThread.class), frame1,
                mock(UserProcessDebugController.class));

        final StackFrame frame2 = mock(StackFrame.class);
        when(frame2.isSuiteFileContext()).thenReturn(false);
        final RobotStackFrame stackFrame2 = new RobotStackFrame(mock(RobotThread.class), frame2,
                mock(UserProcessDebugController.class));

        assertThat(stackFrame1.isSuiteFileContext()).isTrue();
        assertThat(stackFrame2.isSuiteFileContext()).isFalse();
    }

    @Test
    public void labelTest_forDirectorySuiteFrameWithoutLine() {
        final StackFrame dirFrame = mock(StackFrame.class);
        when(dirFrame.isSuiteDirectoryContext()).thenReturn(true);
        when(dirFrame.getName()).thenReturn("frame");

        final RobotStackFrame frame = new RobotStackFrame(mock(RobotThread.class), dirFrame,
                mock(UserProcessDebugController.class));

        assertThat(frame.getLabel()).isEqualTo("[Suite] frame");
    }

    @Test
    public void labelTest_forDirectorySuiteFrameWithLine() {
        final StackFrame dirFrame = mock(StackFrame.class);
        when(dirFrame.isSuiteDirectoryContext()).thenReturn(true);
        when(dirFrame.getName()).thenReturn("frame");
        when(dirFrame.getFileRegion())
                .thenReturn(Optional.of(new FileRegion(new FilePosition(42, 0, 100), new FilePosition(42, 20, 120))));

        final RobotStackFrame frame = new RobotStackFrame(mock(RobotThread.class), dirFrame,
                mock(UserProcessDebugController.class));

        assertThat(frame.getLabel()).isEqualTo("[Suite] frame [line: 42]");
    }

    @Test
    public void labelTest_forFileSuiteFrameWithoutLine() {
        final StackFrame dirFrame = mock(StackFrame.class);
        when(dirFrame.isSuiteFileContext()).thenReturn(true);
        when(dirFrame.getName()).thenReturn("frame");

        final RobotStackFrame frame = new RobotStackFrame(mock(RobotThread.class), dirFrame,
                mock(UserProcessDebugController.class));

        assertThat(frame.getLabel()).isEqualTo("[Suite] frame");
    }

    @Test
    public void labelTest_forFileSuiteFrameWithLine() {
        final StackFrame dirFrame = mock(StackFrame.class);
        when(dirFrame.isSuiteFileContext()).thenReturn(true);
        when(dirFrame.getName()).thenReturn("frame");
        when(dirFrame.getFileRegion())
                .thenReturn(Optional.of(new FileRegion(new FilePosition(42, 0, 100), new FilePosition(42, 20, 120))));

        final RobotStackFrame frame = new RobotStackFrame(mock(RobotThread.class), dirFrame,
                mock(UserProcessDebugController.class));

        assertThat(frame.getLabel()).isEqualTo("[Suite] frame [line: 42]");
    }

    @Test
    public void labelTest_forTestFrameWithoutLine() {
        final StackFrame dirFrame = mock(StackFrame.class);
        when(dirFrame.isTestContext()).thenReturn(true);
        when(dirFrame.getName()).thenReturn("frame");

        final RobotStackFrame frame = new RobotStackFrame(mock(RobotThread.class), dirFrame,
                mock(UserProcessDebugController.class));

        assertThat(frame.getLabel()).isEqualTo("[Test] frame");
    }

    @Test
    public void labelTest_forTestFrameWithLine() {
        final StackFrame dirFrame = mock(StackFrame.class);
        when(dirFrame.isTestContext()).thenReturn(true);
        when(dirFrame.getName()).thenReturn("frame");
        when(dirFrame.getFileRegion())
                .thenReturn(Optional.of(new FileRegion(new FilePosition(42, 0, 100), new FilePosition(42, 20, 120))));

        final RobotStackFrame frame = new RobotStackFrame(mock(RobotThread.class), dirFrame,
                mock(UserProcessDebugController.class));

        assertThat(frame.getLabel()).isEqualTo("[Test] frame [line: 42]");
    }

    @Test
    public void labelTest_forOrdinaryFrameWithoutLine() {
        final StackFrame dirFrame = mock(StackFrame.class);
        when(dirFrame.getName()).thenReturn("frame");

        final RobotStackFrame frame = new RobotStackFrame(mock(RobotThread.class), dirFrame,
                mock(UserProcessDebugController.class));

        assertThat(frame.getLabel()).isEqualTo("frame");
    }

    @Test
    public void labelTest_forOrdinaryFrameWithLine() {
        final StackFrame dirFrame = mock(StackFrame.class);
        when(dirFrame.getName()).thenReturn("frame");
        when(dirFrame.getFileRegion())
                .thenReturn(Optional.of(new FileRegion(new FilePosition(42, 0, 100), new FilePosition(42, 20, 120))));

        final RobotStackFrame frame = new RobotStackFrame(mock(RobotThread.class), dirFrame,
                mock(UserProcessDebugController.class));

        assertThat(frame.getLabel()).isEqualTo("frame [line: 42]");
    }

    @Test
    public void instructionPointerTextIsMadeFromErrorMessageOfErroneousFrame() {
        final StackFrame erroneousFrame = mock(StackFrame.class);
        when(erroneousFrame.isErroneous()).thenReturn(true);
        when(erroneousFrame.getErrorMessage()).thenReturn("error");

        final RobotStackFrame frame = new RobotStackFrame(mock(RobotThread.class), erroneousFrame,
                mock(UserProcessDebugController.class));

        assertThat(frame.getInstructionPointerText()).isEqualTo("error");
    }

    @Test
    public void instructionPointerTextIsEmptyForValidFrame() {
        final StackFrame validFrame = mock(StackFrame.class);
        when(validFrame.isErroneous()).thenReturn(false);

        final RobotStackFrame frame = new RobotStackFrame(mock(RobotThread.class), validFrame,
                mock(UserProcessDebugController.class));

        assertThat(frame.getInstructionPointerText()).isEmpty();
    }

    @Test
    public void frameIsTopFrame_whenThreadReturnsEqualFrameAsTopOne() {
        final Stacktrace stacktrace = new Stacktrace();
        final StackFrame frame = mock(StackFrame.class);
        stacktrace.push(frame);

        final RobotDebugTarget target = mock(RobotDebugTarget.class);
        when(target.isSuspended()).thenReturn(true);

        final RobotThread thread = new RobotThread(target, stacktrace,
                mock(UserProcessDebugController.class));
        final RobotStackFrame stackFrame = new RobotStackFrame(thread, frame, mock(UserProcessDebugController.class));

        assertThat(stackFrame.isTopFrame()).isTrue();
    }

    @Test
    public void frameIsNotATopFrame_whenThreadReturnsNonEqualFrameAsTopOne() {
        final Stacktrace stacktrace = new Stacktrace();
        stacktrace.push(mock(StackFrame.class));

        final RobotDebugTarget target = mock(RobotDebugTarget.class);
        when(target.isSuspended()).thenReturn(true);

        final RobotThread thread = new RobotThread(target, stacktrace, mock(UserProcessDebugController.class));
        final RobotStackFrame stackFrame = new RobotStackFrame(thread, mock(StackFrame.class),
                mock(UserProcessDebugController.class));

        assertThat(stackFrame.isTopFrame()).isFalse();
    }

    @Test
    public void itIsPossibleToStepInto_whenSuspendedAndFrameIsTopOne() {
        final Stacktrace stacktrace = new Stacktrace();
        final StackFrame frame = mock(StackFrame.class);
        stacktrace.push(frame);

        final RobotDebugTarget target = mock(RobotDebugTarget.class);
        when(target.isSuspended()).thenReturn(true);

        final RobotThread thread = new RobotThread(target, stacktrace, mock(UserProcessDebugController.class));
        final RobotStackFrame stackFrame = new RobotStackFrame(thread, frame, mock(UserProcessDebugController.class));

        assertThat(stackFrame.canStepInto()).isTrue();
    }

    @Test
    public void itIsNotPossibleToStepInto_whenSuspendedButFrameIsNotTopOne() {
        final Stacktrace stacktrace = new Stacktrace();
        stacktrace.push(mock(StackFrame.class));

        final RobotDebugTarget target = mock(RobotDebugTarget.class);
        when(target.isSuspended()).thenReturn(true);

        final RobotThread thread = new RobotThread(target, stacktrace, mock(UserProcessDebugController.class));
        final RobotStackFrame stackFrame = new RobotStackFrame(thread, mock(StackFrame.class),
                mock(UserProcessDebugController.class));

        assertThat(stackFrame.canStepInto()).isFalse();
    }

    @Test
    public void itIsNotPossibleToStepInto_whenNotSuspended() {
        final Stacktrace stacktrace = new Stacktrace();
        final StackFrame frame = mock(StackFrame.class);
        stacktrace.push(frame);

        final RobotDebugTarget target = mock(RobotDebugTarget.class);
        when(target.isSuspended()).thenReturn(false);

        final RobotThread thread = new RobotThread(target, stacktrace, mock(UserProcessDebugController.class));
        final RobotStackFrame stackFrame = new RobotStackFrame(thread, frame, mock(UserProcessDebugController.class));

        assertThat(stackFrame.canStepInto()).isFalse();
    }

    @Test
    public void itIsPossibleToStepOverOrReturn_whenSuspended() {
        final Stacktrace stacktrace = new Stacktrace();
        final StackFrame frame = mock(StackFrame.class);
        stacktrace.push(frame);

        final RobotDebugTarget target = mock(RobotDebugTarget.class);
        when(target.isSuspended()).thenReturn(true);

        final RobotThread thread = new RobotThread(target, stacktrace, mock(UserProcessDebugController.class));
        final RobotStackFrame stackFrame = new RobotStackFrame(thread, frame, mock(UserProcessDebugController.class));

        assertThat(stackFrame.canStepOver()).isTrue();
        assertThat(stackFrame.canStepReturn()).isTrue();
    }

    @Test
    public void itIsNotPossibleToStepOverOrReturn_whenNotSuspended() {
        final Stacktrace stacktrace = new Stacktrace();
        final StackFrame frame = mock(StackFrame.class);
        stacktrace.push(frame);

        final RobotDebugTarget target = mock(RobotDebugTarget.class);
        when(target.isSuspended()).thenReturn(false);

        final RobotThread thread = new RobotThread(target, stacktrace, mock(UserProcessDebugController.class));
        final RobotStackFrame stackFrame = new RobotStackFrame(thread, frame, mock(UserProcessDebugController.class));

        assertThat(stackFrame.canStepOver()).isFalse();
        assertThat(stackFrame.canStepReturn()).isFalse();
    }

    @Test
    public void frameIsStepping_whenConstrolerIsStepping() {
        final Stacktrace stacktrace = new Stacktrace();
        final StackFrame frame = mock(StackFrame.class);
        stacktrace.push(frame);

        final UserProcessDebugController controller = mock(UserProcessDebugController.class);
        when(controller.isStepping()).thenReturn(true);

        final RobotStackFrame stackFrame = new RobotStackFrame(
                new RobotThread(mock(RobotDebugTarget.class), stacktrace, controller), frame, controller);

        assertThat(stackFrame.isStepping()).isTrue();
    }

    @Test
    public void frameIsNotStepping_whenConstrolerIsNotStepping() {
        final Stacktrace stacktrace = new Stacktrace();
        final StackFrame frame = mock(StackFrame.class);
        stacktrace.push(frame);

        final UserProcessDebugController controller = mock(UserProcessDebugController.class);
        when(controller.isStepping()).thenReturn(false);

        final RobotStackFrame stackFrame = new RobotStackFrame(
                new RobotThread(mock(RobotDebugTarget.class), stacktrace, controller), frame, controller);

        assertThat(stackFrame.isStepping()).isFalse();
    }

    @Test
    public void stepIntoIsRequestedThroughController() {
        final Stacktrace stacktrace = new Stacktrace();
        final StackFrame frame = mock(StackFrame.class);
        stacktrace.push(frame);

        final UserProcessDebugController controller = mock(UserProcessDebugController.class);
        final RobotStackFrame stackFrame = new RobotStackFrame(
                new RobotThread(mock(RobotDebugTarget.class), stacktrace, controller), frame, controller);

        stackFrame.stepInto();

        verify(controller).stepInto(any(Runnable.class), any(Runnable.class));
        verifyNoMoreInteractions(controller);
    }

    @Test
    public void stepIntoSendsTheCodeToControllerWhichFiresResumeEventsWhenSendToAgent() {
        final Stacktrace stacktrace = new Stacktrace();
        final StackFrame frame = mock(StackFrame.class);
        stacktrace.push(frame);

        final IRobotProcess process = mock(IRobotProcess.class);

        final RobotDebugTarget target = mock(RobotDebugTarget.class);
        when(target.getProcess()).thenReturn(process);

        final RobotThread thread = mock(RobotThread.class);
        when(thread.getDebugTarget()).thenReturn(target);

        final UserProcessDebugController controller = mock(UserProcessDebugController.class);
        final RobotStackFrame stackFrame = new RobotStackFrame(thread, frame, controller);

        final ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);
        stackFrame.stepInto();

        verify(controller).stepInto(captor.capture(), any(Runnable.class));
        verifyNoMoreInteractions(controller);

        final Runnable whenSendRunnable = captor.getValue();
        whenSendRunnable.run();

        verify(process).resumed();
        verify(thread).resumed();
        verify(thread).fireResumeEvent(DebugEvent.STEP_INTO);
    }

    @Test
    public void stepIntoSendsTheCodeToControllerWhichFiresSteppingEndsEventsWhenSuspendedAgain() {
        final Stacktrace stacktrace = new Stacktrace();
        final StackFrame frame = mock(StackFrame.class);
        stacktrace.push(frame);

        final RobotThread thread = mock(RobotThread.class);

        final UserProcessDebugController controller = mock(UserProcessDebugController.class);
        final RobotStackFrame stackFrame = new RobotStackFrame(thread, frame, controller);

        final ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);
        stackFrame.stepInto();

        verify(controller).stepInto(any(Runnable.class), captor.capture());
        verifyNoMoreInteractions(controller);

        final Runnable whenEndsRunnable = captor.getValue();
        whenEndsRunnable.run();

        verify(thread).fireSuspendEvent(DebugEvent.STEP_END);
    }

    @Test
    public void stepOverIsRequestedThroughController() {
        final Stacktrace stacktrace = new Stacktrace();
        final StackFrame frame = mock(StackFrame.class);
        stacktrace.push(frame);

        final UserProcessDebugController controller = mock(UserProcessDebugController.class);
        final RobotStackFrame stackFrame = new RobotStackFrame(
                new RobotThread(mock(RobotDebugTarget.class), stacktrace, controller), frame, controller);

        stackFrame.stepOver();

        verify(controller).stepOver(eq(frame), any(Runnable.class), any(Runnable.class));
        verifyNoMoreInteractions(controller);
    }

    @Test
    public void stepOverSendsTheCodeToControllerWhichFiresResumeEventsWhenSendToAgent() {
        final Stacktrace stacktrace = new Stacktrace();
        final StackFrame frame = mock(StackFrame.class);
        stacktrace.push(frame);

        final IRobotProcess process = mock(IRobotProcess.class);

        final RobotDebugTarget target = mock(RobotDebugTarget.class);
        when(target.getProcess()).thenReturn(process);

        final RobotThread thread = mock(RobotThread.class);
        when(thread.getDebugTarget()).thenReturn(target);

        final UserProcessDebugController controller = mock(UserProcessDebugController.class);
        final RobotStackFrame stackFrame = new RobotStackFrame(thread, frame, controller);

        final ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);
        stackFrame.stepOver();

        verify(controller).stepOver(eq(frame), captor.capture(), any(Runnable.class));
        verifyNoMoreInteractions(controller);

        final Runnable whenSendRunnable = captor.getValue();
        whenSendRunnable.run();

        verify(process).resumed();
        verify(thread).resumed();
        verify(thread).fireResumeEvent(DebugEvent.STEP_OVER);
    }

    @Test
    public void stepOverSendsTheCodeToControllerWhichFiresSteppingEndsEventsWhenSuspendedAgain() {
        final Stacktrace stacktrace = new Stacktrace();
        final StackFrame frame = mock(StackFrame.class);
        stacktrace.push(frame);

        final RobotThread thread = mock(RobotThread.class);

        final UserProcessDebugController controller = mock(UserProcessDebugController.class);
        final RobotStackFrame stackFrame = new RobotStackFrame(thread, frame, controller);

        final ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);
        stackFrame.stepOver();

        verify(controller).stepOver(eq(frame), any(Runnable.class), captor.capture());
        verifyNoMoreInteractions(controller);

        final Runnable whenEndsRunnable = captor.getValue();
        whenEndsRunnable.run();

        verify(thread).fireSuspendEvent(DebugEvent.STEP_END);
    }

    @Test
    public void stepReturnIsRequestedThroughController() {
        final Stacktrace stacktrace = new Stacktrace();
        final StackFrame frame = mock(StackFrame.class);
        stacktrace.push(frame);

        final UserProcessDebugController controller = mock(UserProcessDebugController.class);
        final RobotStackFrame stackFrame = new RobotStackFrame(
                new RobotThread(mock(RobotDebugTarget.class), stacktrace, controller), frame, controller);

        stackFrame.stepReturn();

        verify(controller).stepReturn(eq(frame), any(Runnable.class), any(Runnable.class));
        verifyNoMoreInteractions(controller);
    }

    @Test
    public void stepReturnSendsTheCodeToControllerWhichFiresResumeEventsWhenSendToAgent() {
        final Stacktrace stacktrace = new Stacktrace();
        final StackFrame frame = mock(StackFrame.class);
        stacktrace.push(frame);

        final IRobotProcess process = mock(IRobotProcess.class);

        final RobotDebugTarget target = mock(RobotDebugTarget.class);
        when(target.getProcess()).thenReturn(process);

        final RobotThread thread = mock(RobotThread.class);
        when(thread.getDebugTarget()).thenReturn(target);

        final UserProcessDebugController controller = mock(UserProcessDebugController.class);
        final RobotStackFrame stackFrame = new RobotStackFrame(thread, frame, controller);

        final ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);
        stackFrame.stepReturn();

        verify(controller).stepReturn(eq(frame), captor.capture(), any(Runnable.class));
        verifyNoMoreInteractions(controller);

        final Runnable whenSendRunnable = captor.getValue();
        whenSendRunnable.run();

        verify(process).resumed();
        verify(thread).resumed();
        verify(thread).fireResumeEvent(DebugEvent.STEP_RETURN);
    }

    @Test
    public void stepReturnSendsTheCodeToControllerWhichFiresSteppingEndsEventsWhenSuspendedAgain() {
        final Stacktrace stacktrace = new Stacktrace();
        final StackFrame frame = mock(StackFrame.class);
        stacktrace.push(frame);

        final RobotThread thread = mock(RobotThread.class);

        final UserProcessDebugController controller = mock(UserProcessDebugController.class);
        final RobotStackFrame stackFrame = new RobotStackFrame(thread, frame, controller);

        final ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);
        stackFrame.stepReturn();

        verify(controller).stepReturn(eq(frame), any(Runnable.class), captor.capture());
        verifyNoMoreInteractions(controller);

        final Runnable whenEndsRunnable = captor.getValue();
        whenEndsRunnable.run();

        verify(thread).fireSuspendEvent(DebugEvent.STEP_END);
    }

    @Test
    public void frameHasVariablesWhenIsSuspended() {
        final Stacktrace stacktrace = new Stacktrace();
        final StackFrame frame = mock(StackFrame.class);
        stacktrace.push(frame);

        final RobotDebugTarget target = mock(RobotDebugTarget.class);
        when(target.isSuspended()).thenReturn(true);

        final RobotThread thread = new RobotThread(target, stacktrace, mock(UserProcessDebugController.class));
        final RobotStackFrame stackFrame = new RobotStackFrame(thread, frame, mock(UserProcessDebugController.class));

        assertThat(stackFrame.hasVariables()).isTrue();
    }

    @Test
    public void frameHasNoVariablesWhenNotSuspended() {
        final Stacktrace stacktrace = new Stacktrace();
        final StackFrame frame = mock(StackFrame.class);
        stacktrace.push(frame);

        final RobotDebugTarget target = mock(RobotDebugTarget.class);
        when(target.isSuspended()).thenReturn(false);

        final RobotThread thread = new RobotThread(target, stacktrace, mock(UserProcessDebugController.class));
        final RobotStackFrame stackFrame = new RobotStackFrame(thread, frame, mock(UserProcessDebugController.class));

        assertThat(stackFrame.hasVariables()).isFalse();
    }

    @Test
    public void variablesForFrameAreProvidedWithoutMarkedChanges_whenThereIsNoDeltaInFrame() {
        final Map<Variable, VariableTypedValue> variables = new LinkedHashMap<>();
        variables.put(new Variable("var", VariableScope.GLOBAL), new VariableTypedValue("str", "abc"));
        variables.put(new Variable("list", VariableScope.TEST_SUITE), new VariableTypedValue("list", newArrayList()));
        variables.put(new Variable("${true}", VariableScope.GLOBAL), new VariableTypedValue("bool", true));

        final StackFrameVariables vars = StackFrameVariables.newNonLocalVariables(variables);
        
        final Stacktrace stacktrace = new Stacktrace();
        final StackFrame frame = mock(StackFrame.class);
        when(frame.getVariables()).thenReturn(vars);
        stacktrace.push(frame);

        final RobotThread thread = new RobotThread(mock(RobotDebugTarget.class), stacktrace,
                mock(UserProcessDebugController.class));
        final RobotStackFrame stackFrame = new RobotStackFrame(thread, frame, mock(UserProcessDebugController.class));
        
        final RobotDebugVariable[] robotVariables = stackFrame.getVariables();
        assertThat(robotVariables).hasSize(3);

        assertThat(robotVariables[0].getName()).isEqualTo("var");
        assertThat(robotVariables[0].getScope()).contains(VariableScope.GLOBAL);
        assertThat(robotVariables[0].getValue().getValueString()).isEqualTo("abc");
        assertThat(robotVariables[0].getValue().getReferenceTypeName()).isEqualTo("str");
        assertThat(robotVariables[0].hasValueChanged()).isFalse();

        assertThat(robotVariables[1].getName()).isEqualTo("list");
        assertThat(robotVariables[1].getScope()).contains(VariableScope.TEST_SUITE);
        assertThat(robotVariables[1].getValue().getValueString()).isEqualTo("list[0]");
        assertThat(robotVariables[1].getValue().getReferenceTypeName()).isEqualTo("list");
        assertThat(robotVariables[1].hasValueChanged()).isFalse();

        assertThat(robotVariables[2].getName()).isEqualTo(RobotDebugVariable.AUTOMATIC_NAME);
        assertThat(robotVariables[2].getScope()).isEmpty();
        assertThat(robotVariables[2].getValue().getValueString()).isEmpty();
        assertThat(robotVariables[2].getValue().getReferenceTypeName()).isEmpty();
        assertThat(robotVariables[2].hasValueChanged()).isFalse();

        assertThat(robotVariables[2].getValue().getVariables()).hasSize(1);
        final RobotDebugVariable innerAutomaticVar = robotVariables[2].getValue().getVariables()[0];

        assertThat(innerAutomaticVar.getName()).isEqualTo("${true}");
        assertThat(innerAutomaticVar.getScope()).contains(VariableScope.GLOBAL);
        assertThat(innerAutomaticVar.getValue().getValueString()).isEqualTo("true");
        assertThat(innerAutomaticVar.getValue().getReferenceTypeName()).isEqualTo("bool");
        assertThat(innerAutomaticVar.hasValueChanged()).isFalse();

        final List<String> allVars = stackFrame.getAllVariables().stream().map(RobotDebugVariable::getName).collect(toList());
        assertThat(allVars).containsExactly("var", "list", RobotDebugVariable.AUTOMATIC_NAME, "${true}");
    }

    @Test
    public void variablesForFrameAreProvidedSorted_whenSortingIsEnabled() {
        final PersistentState state = provideSortingState();
        state.setValue(true);

        final Map<Variable, VariableTypedValue> variables = new LinkedHashMap<>();
        variables.put(new Variable("var", VariableScope.GLOBAL), new VariableTypedValue("str", "abc"));
        variables.put(new Variable("list", VariableScope.TEST_SUITE), new VariableTypedValue("list", newArrayList()));
        variables.put(new Variable("@{test_tags}", VariableScope.GLOBAL), new VariableTypedValue("list", newArrayList()));
        variables.put(new Variable("${false}", VariableScope.GLOBAL), new VariableTypedValue("bool", true));

        final StackFrameVariables vars = StackFrameVariables.newNonLocalVariables(variables);

        final Stacktrace stacktrace = new Stacktrace();
        final StackFrame frame = mock(StackFrame.class);
        when(frame.getVariables()).thenReturn(vars);
        stacktrace.push(frame);

        final RobotThread thread = new RobotThread(mock(RobotDebugTarget.class), stacktrace,
                mock(UserProcessDebugController.class));
        final RobotStackFrame stackFrame = new RobotStackFrame(thread, frame, mock(UserProcessDebugController.class));

        final RobotDebugVariable[] robotVariables = stackFrame.getVariables();
        assertThat(robotVariables).hasSize(3);

        assertThat(robotVariables[0].getName()).isEqualTo("list");
        assertThat(robotVariables[1].getName()).isEqualTo("var");
        assertThat(robotVariables[2].getName()).isEqualTo(RobotDebugVariable.AUTOMATIC_NAME);
        final RobotDebugVariable[] innerAutomaticVars = robotVariables[2].getValue().getVariables();
        assertThat(innerAutomaticVars[0].getName()).isEqualTo("${false}");
        assertThat(innerAutomaticVars[1].getName()).isEqualTo("@{test_tags}");

        state.setValue(false);
    }

    @Test
    public void variablesForFrameAreProvidedWithMarkedChanges_whenThereIsADeltaInFrame() {
        final Map<Variable, VariableTypedValue> variables = new LinkedHashMap<>();
        variables.put(new Variable("var", VariableScope.GLOBAL), new VariableTypedValue("str", "abc"));
        variables.put(new Variable("list", VariableScope.TEST_SUITE), new VariableTypedValue("list", newArrayList()));
        variables.put(new Variable("${true}", VariableScope.GLOBAL), new VariableTypedValue("bool", true));
        final StackFrameVariables vars = StackFrameVariables.newNonLocalVariables(variables);

        final StackVariablesDelta delta = mock(StackVariablesDelta.class);
        when(delta.isAdded("var")).thenReturn(true);
        when(delta.isChanged("${true}")).thenReturn(true);

        final Stacktrace stacktrace = new Stacktrace();
        final StackFrame frame = mock(StackFrame.class);
        when(frame.getVariables()).thenReturn(vars);
        when(frame.getLastDelta()).thenReturn(Optional.of(delta));
        stacktrace.push(frame);

        final RobotThread thread = new RobotThread(mock(RobotDebugTarget.class), stacktrace,
                mock(UserProcessDebugController.class));
        final RobotStackFrame stackFrame = new RobotStackFrame(thread, frame, mock(UserProcessDebugController.class));

        final RobotDebugVariable[] robotVariables = stackFrame.getVariables();
        assertThat(robotVariables).hasSize(3);

        assertThat(robotVariables[0].hasValueChanged()).isTrue();
        assertThat(robotVariables[1].hasValueChanged()).isFalse();
        assertThat(robotVariables[2].hasValueChanged()).isTrue();

        final RobotDebugVariable innerAutomaticVar = robotVariables[2].getValue().getVariables()[0];
        assertThat(innerAutomaticVar.hasValueChanged()).isTrue();
    }

    @Test
    public void variableChangeRequestArePassedToController() {
        final Stacktrace stacktrace = new Stacktrace();
        final StackFrame frame = mock(StackFrame.class);
        stacktrace.push(frame);

        final UserProcessDebugController controller = mock(UserProcessDebugController.class);
        final RobotDebugTarget target = new RobotDebugTarget("target", null, stacktrace, controller);

        final RobotThread thread = new RobotThread(target, stacktrace, controller);
        final RobotStackFrame stackFrame = new RobotStackFrame(thread, frame, controller);

        final StackFrameVariable variable = new StackFrameVariable(VariableScope.GLOBAL, false, "var", "int", 10);
        final List<String> args = newArrayList("new value");
        stackFrame.changeVariable(variable, args);

        verify(controller).changeVariable(frame, variable, args);
    }

    @Test
    public void innerVariableChangeRequestArePassedToDebugTarget() {
        final Stacktrace stacktrace = new Stacktrace();
        final StackFrame frame = mock(StackFrame.class);
        stacktrace.push(frame);

        final UserProcessDebugController controller = mock(UserProcessDebugController.class);
        final RobotDebugTarget target = new RobotDebugTarget("target", null, stacktrace, controller);

        final RobotThread thread = new RobotThread(target, stacktrace, controller);
        final RobotStackFrame stackFrame = new RobotStackFrame(thread, frame, controller);

        final StackFrameVariable variable = new StackFrameVariable(VariableScope.GLOBAL, false, "var", "int", 10);
        final List<String> args = newArrayList("new value");
        final List<Object> path = newArrayList("s1", "s2");
        stackFrame.changeVariableInnerValue(variable, path, args);

        verify(controller).changeVariableInnerValue(frame, variable, path, args);
    }

    private static PersistentState provideSortingState() {
        final ICommandService commandService = PlatformUI.getWorkbench().getService(ICommandService.class);
        final Command command = commandService.getCommand(AlwaysDisplaySortedVariablesHandler.COMMAND_ID);
        return (PersistentState) command.getState(AlwaysDisplaySortedVariablesHandler.COMMAND_STATE_ID);
    }
}
