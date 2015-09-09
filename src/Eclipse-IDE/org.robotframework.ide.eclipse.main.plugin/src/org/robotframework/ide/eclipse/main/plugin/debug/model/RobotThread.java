/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see licence.txt file for details.
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
    public RobotThread(RobotDebugTarget target) {
        super(target);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.IThread#getStackFrames()
     */
    public IStackFrame[] getStackFrames() throws DebugException {
        if (isSuspended()) {
            return ((RobotDebugTarget) getDebugTarget()).getStackFrames();
        } else {
            return new IStackFrame[0];
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.IThread#hasStackFrames()
     */
    public boolean hasStackFrames() throws DebugException {
        return isSuspended();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.IThread#getPriority()
     */
    public int getPriority() throws DebugException {
        return 0;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.IThread#getTopStackFrame()
     */
    public IStackFrame getTopStackFrame() throws DebugException {
        IStackFrame[] frames = getStackFrames();
        if (frames.length > 0) {
            return frames[0];
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.IThread#getName()
     */
    public String getName() throws DebugException {
        return "Thread [main]";
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.IThread#getBreakpoints()
     */
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
    public void setBreakpoints(IBreakpoint[] breakpoints) {
        this.breakpoints = breakpoints;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.ISuspendResume#canResume()
     */
    public boolean canResume() {
        return isSuspended();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.ISuspendResume#canSuspend()
     */
    public boolean canSuspend() {
        return !isSuspended() && !isTerminated();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.ISuspendResume#isSuspended()
     */
    public boolean isSuspended() {
        return getDebugTarget().isSuspended();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.ISuspendResume#resume()
     */
    public void resume() throws DebugException {
        getDebugTarget().resume();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.ISuspendResume#suspend()
     */
    public void suspend() throws DebugException {
        getDebugTarget().suspend();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.IStep#canStepInto()
     */
    public boolean canStepInto() {
        return isSuspended();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.IStep#canStepOver()
     */
    public boolean canStepOver() {
        return isSuspended();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.IStep#canStepReturn()
     */
    public boolean canStepReturn() {
        return isSuspended() && ((RobotDebugTarget) getDebugTarget()).getCurrentFrames().size() > 1;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.IStep#isStepping()
     */
    public boolean isStepping() {
        return isStepping;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.IStep#stepInto()
     */
    public void stepInto() throws DebugException {
        ((RobotDebugTarget) getDebugTarget()).step();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.IStep#stepOver()
     */
    public void stepOver() throws DebugException {
        isSteppingOver = true;
        ((RobotDebugTarget) getDebugTarget()).stepOver();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.IStep#stepReturn()
     */
    public void stepReturn() throws DebugException {
        isSteppingReturn = true;
        ((RobotDebugTarget) getDebugTarget()).stepReturn();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.ITerminate#canTerminate()
     */
    public boolean canTerminate() {
        return !isTerminated();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.ITerminate#isTerminated()
     */
    public boolean isTerminated() {
        return getDebugTarget().isTerminated();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.ITerminate#terminate()
     */
    public void terminate() throws DebugException {
        getDebugTarget().terminate();
    }

    /**
     * Sets whether this thread is stepping
     * 
     * @param stepping
     *            whether stepping
     */
    protected void setStepping(boolean stepping) {
        isStepping = stepping;
    }
    
    public void setSteppingOver(boolean stepping) {
        isSteppingOver = stepping;
    }
    
    public boolean isSteppingOver() {
        return isSteppingOver;
    }
    
    public void setSteppingReturn(boolean stepping) {
        isSteppingReturn = stepping;
    }
    
    public boolean isSteppingReturn() {
        return isSteppingReturn;
    }
}
