/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.debug.model;

import java.util.List;
import java.util.function.Consumer;

import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IThread;
import org.rf.ide.core.execution.debug.RobotLineBreakpoint;
import org.rf.ide.core.execution.debug.StackFrame;
import org.rf.ide.core.execution.debug.StackFrameVariable;
import org.rf.ide.core.execution.debug.Stacktrace;
import org.rf.ide.core.execution.debug.UserProcessDebugController;
import org.rf.ide.core.execution.debug.UserProcessDebugController.PauseReasonListener;
import org.robotframework.ide.eclipse.main.plugin.launch.IRobotProcess;

import com.google.common.annotations.VisibleForTesting;

public class RobotDebugTarget extends RobotDebugElement implements IDebugTarget {

    private final ILaunch launch;

    private IRobotProcess process;

    private RobotThread singleThread;

    private final String name;

    private final Stacktrace stacktrace;

    private final UserProcessDebugController userController;

    public RobotDebugTarget(final String name, final ILaunch launch, final Stacktrace stacktrace,
            final UserProcessDebugController userController) {
        this(name, launch, stacktrace, userController, RobotDebugElement::fireEvent);
    }

    @VisibleForTesting
    RobotDebugTarget(final String name, final ILaunch launch, final Stacktrace stacktrace,
            final UserProcessDebugController userController, final Consumer<DebugEvent> eventsNotifier) {
        super(null, eventsNotifier);
        this.name = name;
        this.launch = launch;
        this.stacktrace = stacktrace;
        this.userController = userController;
        this.userController.whenSuspended(new ExecutionPauseReasonsListener());
    }

    public void connected() {
        setThread(new RobotThread(this, stacktrace, userController));
        fireChangeEvent(DebugEvent.STATE);
    }

    @Override
    public IRobotProcess getProcess() {
        return process;
    }

    public void setProcess(final IRobotProcess process) {
        this.process = process;
    }

    @Override
    public boolean hasThreads() {
        return singleThread != null;
    }

    @Override
    public IThread[] getThreads() {
        return hasThreads() ? new IThread[] { singleThread } : new IThread[0];
    }

    public RobotThread getThread() {
        return singleThread;
    }

    @VisibleForTesting
    void setThread(final RobotThread thread) {
        this.singleThread = thread;
    }

    @Override
    public String getName() {
        return (isSuspended() ? "<suspended>" : "") + name;
    }

    @Override
    public ILaunch getLaunch() {
        return launch;
    }

    public void interrupt() {
        if (isSuspended()) {
            getThread().resumed();
            getThread().fireResumeEvent(DebugEvent.CLIENT_REQUEST);

            fireChangeEvent(DebugEvent.CONTENT);
        }
        getProcess().interrupt();
    }

    @Override
    public boolean canDisconnect() {
        return getProcess().canDisconnect();
    }

    @Override
    public boolean isDisconnected() {
        return getProcess().isDisconnected();
    }

    @Override
    public void disconnect() {
        singleThread = null;
        getProcess().disconnect();

        // this is needed because eclipse source facility is clearing
        // current instruction pointers annotations in opened editors
        // only on resume/terminate events
        fireTerminateEvent();
    }

    @Override
    public boolean canTerminate() {
        return getProcess().canTerminate();
    }

    @Override
    public boolean isTerminated() {
        return getProcess().isTerminated();
    }

    @Override
    public void terminate() throws DebugException {
        singleThread = null;
        getProcess().terminate();

        fireTerminateEvent();
    }

    @Override
    public boolean canSuspend() {
        return getProcess().canSuspend();
    }

    @Override
    public boolean isSuspended() {
        return getProcess().isSuspended();
    }

    @Override
    public void suspend() {
        getProcess().suspend();
    }

    @Override
    public boolean canResume() {
        return getProcess().canResume();
    }

    @Override
    public void resume() {
        getProcess().resume();

        getThread().resumed();
        getThread().fireResumeEvent(DebugEvent.CLIENT_REQUEST);

        fireChangeEvent(DebugEvent.CONTENT);
    }

    void changeVariable(final StackFrame frame, final StackFrameVariable variable, final List<String> arguments) {
        userController.changeVariable(frame, variable, arguments);
    }

    void changeVariableInnerValue(final StackFrame frame, final StackFrameVariable variable, final List<Object> path,
            final List<String> arguments) {
        userController.changeVariableInnerValue(frame, variable, path, arguments);
    }

    @Override
    public void breakpointAdded(final IBreakpoint breakpoint) {
        // nothing to do
    }

    @Override
    public void breakpointRemoved(final IBreakpoint breakpoint, final IMarkerDelta delta) {
        // nothing to do
    }

    @Override
    public void breakpointChanged(final IBreakpoint breakpoint, final IMarkerDelta delta) {
        // nothing to do
    }

    @Override
    public boolean supportsBreakpoint(final IBreakpoint breakpoint) {
        return breakpoint.getModelIdentifier().equals(RobotDebugElement.DEBUG_MODEL_ID);
    }

    /**
     * Set the thread's breakpoint
     * 
     * @param breakpoint
     */
    public void breakpointHit(final IBreakpoint breakpoint) {
        if (breakpoint instanceof ILineBreakpoint) {
            getThread().suspendedAt(breakpoint);
        }
    }

    @Override
    public boolean supportsStorageRetrieval() {
        return false;
    }

    @Override
    public IMemoryBlock getMemoryBlock(final long startAddress, final long length) {
        return null;
    }

    @VisibleForTesting
    class ExecutionPauseReasonsListener implements PauseReasonListener {

        @Override
        public void pausedOnBreakpoint(final RobotLineBreakpoint breakpoint) {
            suspended(DebugEvent.BREAKPOINT);
            breakpointHit((IBreakpoint) breakpoint);
        }

        @Override
        public void pausedByUser() {
            suspended(DebugEvent.CLIENT_REQUEST);
        }

        @Override
        public void pausedByStepping() {
            suspended(DebugEvent.STEP_END);
        }

        @Override
        public void pausedOnError(final String error) {
            suspended(DebugEvent.CLIENT_REQUEST);
        }

        @Override
        public void pausedAfterVariableChange(final int frameLevel) {
            suspended(DebugEvent.EVALUATION);
        }

        private void suspended(final int detail) {
            getProcess().suspended();
            getThread().fireSuspendEvent(detail);
        }
    }
}
