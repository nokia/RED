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
import org.rf.ide.core.execution.debug.Stacktrace;
import org.rf.ide.core.execution.debug.UserProcessDebugController;

public class RobotThread extends RobotDebugElement implements IThread {

    // breakpoints this thread is suspended at
    private final List<IBreakpoint> breakpoints;

    private final UserProcessDebugController userController;

    private final Stacktrace stacktrace;

    public RobotThread(final RobotDebugTarget target, final Stacktrace stacktrace,
            final UserProcessDebugController userController) {
        super(target);
        this.breakpoints = new ArrayList<>();
        this.userController = userController;
        this.stacktrace = stacktrace;
    }

    @Override
    public boolean hasStackFrames() {
        return isSuspended() && !stacktrace.isEmpty();
    }

    @Override
    public synchronized RobotStackFrame[] getStackFrames() {
        return stacktrace.stream()
                .map(f -> new RobotStackFrame(this, f, userController))
                .toArray(RobotStackFrame[]::new);
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public RobotStackFrame getTopStackFrame() {
        return hasStackFrames() ? new RobotStackFrame(this, stacktrace.iterator().next(), userController) : null;
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

    public void resumed() {
        breakpoints.clear();
    }

    @Override
    public boolean isStepping() {
        return userController.isStepping();
    }

    @Override
    public boolean canStepInto() {
        return isSuspended() && !stacktrace.isEmpty();
    }

    @Override
    public boolean canStepOver() {
        return isSuspended() && !stacktrace.isEmpty();
    }

    @Override
    public boolean canStepReturn() {
        return isSuspended() && !stacktrace.isEmpty();
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
