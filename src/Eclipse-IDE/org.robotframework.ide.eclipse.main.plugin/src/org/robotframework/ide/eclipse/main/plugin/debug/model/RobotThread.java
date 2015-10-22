/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.debug.model;

import org.eclipse.debug.core.DebugException;
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

    /**
     * Whether this thread is stepping
     */
    private boolean isStepping = false;
    
    private boolean isSteppingOver = false;
    
    private boolean isSteppingReturn = false;

    /**
     * Constructs a new thread for the given target
     * 
     * @param target
     * 
     */
    public RobotThread(final RobotDebugTarget target) {
        super(target);
    }

    @Override
    public IStackFrame[] getStackFrames() throws DebugException {

        IStackFrame[] stackFrames = ((RobotDebugTarget) getDebugTarget()).getStackFrames();
        if (stackFrames == null) {
            return new IStackFrame[0];
        }

        return stackFrames;
    }

    @Override
    public boolean hasStackFrames() throws DebugException {
        return isSuspended();
    }

    @Override
    public int getPriority() throws DebugException {
        return 0;
    }

    @Override
    public IStackFrame getTopStackFrame() throws DebugException {
        final IStackFrame[] frames = getStackFrames();
        if (frames.length > 0) {
            return frames[0];
        }
        return null;
    }

    @Override
    public String getName() throws DebugException {
        return "Thread [main]";
    }

    @Override
    public IBreakpoint[] getBreakpoints() {
        if (breakpoints == null) {
            return new IBreakpoint[0];
        }
        return breakpoints;
    }

    /**
     * Sets the breakpoints this thread is suspended at, or <code>null</code> if none.
     * 
     * @param breakpoints
     *            the breakpoints this thread is suspended at, or <code>null</code> if none
     */
    public void setBreakpoints(final IBreakpoint[] breakpoints) {
        this.breakpoints = breakpoints;
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
    public void resume() throws DebugException {
        getDebugTarget().resume();
    }

    @Override
    public void suspend() throws DebugException {
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
        return isSuspended() && ((RobotDebugTarget) getDebugTarget()).getCurrentKeywordsContextMap().size() > 1;
    }

    @Override
    public boolean isStepping() {
        return isStepping;
    }

    @Override
    public void stepInto() throws DebugException {
        ((RobotDebugTarget) getDebugTarget()).step();
    }

    @Override
    public void stepOver() throws DebugException {
        isSteppingOver = true;
        ((RobotDebugTarget) getDebugTarget()).stepOver();
    }

    @Override
    public void stepReturn() throws DebugException {
        isSteppingReturn = true;
        ((RobotDebugTarget) getDebugTarget()).stepReturn();
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
    public void terminate() throws DebugException {
        getDebugTarget().terminate();
    }

    /**
     * Sets whether this thread is stepping
     * 
     * @param stepping
     *            whether stepping
     */
    protected void setStepping(final boolean stepping) {
        isStepping = stepping;
    }
    
    public void setSteppingOver(final boolean stepping) {
        isSteppingOver = stepping;
    }
    
    public boolean isSteppingOver() {
        return isSteppingOver;
    }
    
    public void setSteppingReturn(final boolean stepping) {
        isSteppingReturn = stepping;
    }
    
    public boolean isSteppingReturn() {
        return isSteppingReturn;
    }
}
