/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.debug.model;

import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;

/**
 * @author mmarzec
 *
 */
public class RobotThread extends RobotDebugElement implements IThread {

    /**
     * Breakpoints this thread is suspended at or <code>null</code> if none.
     */
    private IBreakpoint[] breakpoints;

    private boolean isStepping = false;
    private boolean isSteppingOver = false;
    private boolean isSteppingReturn = false;

    public RobotThread(final RobotDebugTarget target) {
        super(target);
    }

    @Override
    public RobotDebugTarget getDebugTarget() {
        return (RobotDebugTarget) super.getDebugTarget();
    }

    @Override
    public IStackFrame[] getStackFrames() {
        final IStackFrame[] stackFrames = getDebugTarget().getStackFrames();
        return stackFrames == null ? new IStackFrame[0] : stackFrames;
    }

    @Override
    public boolean hasStackFrames() {
        return isSuspended();
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public IStackFrame getTopStackFrame() {
        final IStackFrame[] frames = getStackFrames();
        return frames.length > 0 ? frames[0] : null;
    }

    @Override
    public String getName() {
        return "Thread [main]";
    }

    @Override
    public IBreakpoint[] getBreakpoints() {
        return breakpoints == null ? new IBreakpoint[0] : breakpoints;
    }

    void setSuspendedAt(final IBreakpoint breakpoint) {
        this.breakpoints = new IBreakpoint[] { breakpoint };
    }

    @Override
    public boolean canResume() {
        return isSuspended();
    }

    @Override
    public boolean canSuspend() {
        return !isSuspended() && !isTerminated();
    }

    @Override
    public boolean isSuspended() {
        return getDebugTarget().isSuspended();
    }

    @Override
    public void resume() {
        getDebugTarget().resume();
    }

    @Override
    public void suspend() {
        getDebugTarget().suspend();
    }

    @Override
    public boolean canStepInto() {
        return isSuspended();
    }

    @Override
    public boolean canStepOver() {
        return isSuspended();
    }

    @Override
    public boolean canStepReturn() {
        return isSuspended() && getDebugTarget().getCurrentKeywordsContextMap().size() > 1;
    }

    @Override
    public boolean isStepping() {
        return isStepping;
    }

    void setStepping(final boolean stepping) {
        isStepping = stepping;
    }

    public void setSteppingOver(final boolean stepping) {
        isSteppingOver = stepping;
    }

    boolean isSteppingOver() {
        return isSteppingOver;
    }

    public void setSteppingReturn(final boolean stepping) {
        isSteppingReturn = stepping;
    }

    boolean isSteppingReturn() {
        return isSteppingReturn;
    }

    @Override
    public void stepInto() {
        getDebugTarget().step();
    }

    @Override
    public void stepOver() {
        isSteppingOver = true;
        getDebugTarget().stepOver();
    }

    @Override
    public void stepReturn() {
        isSteppingReturn = true;
        getDebugTarget().stepReturn();
    }

    @Override
    public boolean canTerminate() {
        return !isTerminated();
    }

    @Override
    public boolean isTerminated() {
        return getDebugTarget().isTerminated();
    }

    @Override
    public void terminate() {
        getDebugTarget().terminate();
    }
}
