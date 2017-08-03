/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.debug.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IThread;
import org.rf.ide.core.execution.debug.StackFrame;
import org.rf.ide.core.execution.debug.Stacktrace;
import org.rf.ide.core.execution.debug.Stacktrace.StacktraceListener;
import org.rf.ide.core.execution.debug.UserProcessDebugController;

public class RobotThread extends RobotDebugElement implements IThread {

    // breakpoints this thread is suspended at
    private final List<IBreakpoint> breakpoints;

    private final UserProcessDebugController userController;

    private final Stacktrace stacktrace;

    private final List<RobotStackFrame> frames;

    public RobotThread(final RobotDebugTarget target, final Stacktrace stacktrace,
            final UserProcessDebugController userController) {
        super(target);
        this.breakpoints = new ArrayList<>();
        this.userController = userController;
        this.stacktrace = stacktrace;
        this.frames = new ArrayList<>();
        this.stacktrace.addListener(new StacktraceListener() {

            @Override
            public void framePushed(final Stacktrace stack, final StackFrame frame) {
                final RobotStackFrame newFrame = new RobotStackFrame(RobotThread.this, frame, userController);
                newFrame.createVariables();
                frames.add(0, newFrame);
            }

            @Override
            public void framePopped(final Stacktrace stack, final StackFrame frame) {
                frames.remove(0);
            }
        });
    }

    @Override
    public boolean hasStackFrames() {
        return isSuspended() && !stacktrace.isEmpty();
    }

    @Override
    public RobotStackFrame[] getStackFrames() {
        return frames.toArray(new RobotStackFrame[0]);
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public RobotStackFrame getTopStackFrame() {
        final RobotStackFrame[] frames = getStackFrames();
        return frames != null && frames.length > 0 ? frames[0] : null;
    }

    @Override
    public String getName() {
        return "Tests execution thread";
    }

    @Override
    public IBreakpoint[] getBreakpoints() {
        return breakpoints.toArray(new IBreakpoint[0]);
    }

    public void suspendedAt(final IBreakpoint breakpoint) {
        breakpoints.clear();
        breakpoints.add(breakpoint);
    }

    void resumedFromBreakpoint() {
        breakpoints.clear();
    }

    @Override
    public boolean isStepping() {
        return userController.isStepping();
    }

    @Override
    public boolean canStepInto() {
        return isSuspended() && !stacktrace.isEmpty() && getTopStackFrame().canStepInto();
    }

    @Override
    public boolean canStepOver() {
        return isSuspended() && !stacktrace.isEmpty() && getTopStackFrame().canStepOver();
    }

    @Override
    public boolean canStepReturn() {
        return isSuspended() && !stacktrace.isEmpty() && getTopStackFrame().canStepReturn();
    }

    @Override
    public void stepInto() {
        getTopStackFrame().stepInto();
    }

    @Override
    public void stepOver() {
        getTopStackFrame().stepOver();
    }

    @Override
    public void stepReturn() {
        getTopStackFrame().stepReturn();
    }
}
