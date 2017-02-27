/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.debug.model;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.rf.ide.core.execution.server.AgentClient;
import org.robotframework.ide.eclipse.main.plugin.debug.utils.KeywordContext;
import org.robotframework.ide.eclipse.main.plugin.debug.utils.RobotDebugStackFrameManager;
import org.robotframework.ide.eclipse.main.plugin.debug.utils.RobotDebugValueManager;
import org.robotframework.ide.eclipse.main.plugin.debug.utils.RobotDebugVariablesManager;

import com.google.common.collect.Iterables;

/**
 * @author mmarzec
 */
@SuppressWarnings({ "PMD.TooManyFields", "PMD.TooManyMethods", "PMD.GodClass" })
public class RobotDebugTarget extends RobotDebugElement implements IDebugTarget {

    // associated system process (Robot)
    private IProcess process;

    // containing launch object
    private final ILaunch launch;

    // program name
    private final String name;
    
    // suspend state
    private boolean isSuspended;

    private IThread[] threads;

    private final Map<String, KeywordContext> currentKeywordDebugContextMap;

    private int currentStepOverLevel;
    
    private int currentStepReturnLevel;

    private final RobotDebugVariablesManager robotVariablesManager;
    
    private final RobotDebugValueManager robotDebugValueManager;
    
    private RobotDebugStackFrameManager robotDebugStackFrameManager;
    
    private UserController userController;

    public RobotDebugTarget(final String name, final ILaunch launch) {
        super(null);
        this.name = name;
        this.launch = launch;
        this.currentKeywordDebugContextMap = new LinkedHashMap<>();
        this.robotVariablesManager = new RobotDebugVariablesManager(this);
        this.robotDebugValueManager = new RobotDebugValueManager();

        this.threads = null;

        this.isSuspended = false;
        this.currentStepOverLevel = 0;
        this.currentStepReturnLevel = 0;

        this.robotDebugStackFrameManager = null;
    }

    public void connectWith(final IProcess process) {
        this.process = process;

        threads = new IThread[] { new RobotThread(this) };
        robotDebugStackFrameManager = new RobotDebugStackFrameManager(getThread());

        DebugPlugin.getDefault().getBreakpointManager().addBreakpointListener(this);

        launch.addDebugTarget(this);
    }

    public void setClient(final AgentClient client) {
        userController = new UserController(robotVariablesManager, client);
    }

    @Override
    public IProcess getProcess() {
        return process;
    }

    @Override
    public IThread[] getThreads() {
        return threads;
    }

    private RobotThread getThread() {
        return threads.length == 0 ? null : (RobotThread) threads[0];
    }

    @Override
    public boolean hasThreads() {
        return true;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public IDebugTarget getDebugTarget() {
        return this;
    }

    @Override
    public ILaunch getLaunch() {
        return launch;
    }

    @Override
    public boolean canDisconnect() {
        return false;
    }

    @Override
    public boolean isDisconnected() {
        return false;
    }

    @Override
    public void disconnect() {

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
    public void terminate() {
        // if (eventSocket != null) {
        // userController.interrupt();
        // }
        terminated();
    }

    @Override
    public boolean canSuspend() {
        return !isTerminated() && !isSuspended();
    }

    @Override
    public boolean isSuspended() {
        return isSuspended;
    }

    @Override
    public void suspend() {

    }

    @Override
    public boolean canResume() {
        return !isTerminated() && isSuspended();
    }

    @Override
    public void resume() {
        getThread().setStepping(false);
        userController.resume();
        resumed(DebugEvent.CLIENT_REQUEST);
    }

    protected void step() {
        getThread().setStepping(true);
        userController.resume();
        resumed(DebugEvent.CLIENT_REQUEST);
    }

    protected void stepOver() {
        currentStepOverLevel = currentKeywordDebugContextMap.size();
        step();
    }
    
    protected void stepReturn() {
        currentStepReturnLevel = currentKeywordDebugContextMap.size();
        step();
    }

    /**
     * Notification the target has resumed for the given reason
     * 
     * @param detail
     *            reason for the resume
     */
    private void resumed(final int detail) {
        isSuspended = false;
        getThread().fireResumeEvent(detail);
    }

    /**
     * Notification the target has suspended for the given reason
     * 
     * @param detail
     *            reason for the suspend
     */
    public void suspended(final int detail) {
        isSuspended = true;
        getThread().fireSuspendEvent(detail);
    }

    /**
     * Notification we have connected to the Agent and it has started.
     */
    public void started() {
        fireCreationEvent();
    }

    /**
     * Called when this debug target terminates.
     */
    public void terminated() {
        isSuspended = false;
        DebugPlugin.getDefault().getBreakpointManager().removeBreakpointListener(this);
        fireTerminateEvent();
       
        if(getProcess() != null) {
            try {
                getProcess().terminate();
            } catch (final DebugException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void breakpointAdded(final IBreakpoint breakpoint) {

    }

    @Override
    public void breakpointRemoved(final IBreakpoint breakpoint, final IMarkerDelta delta) {

    }

    @Override
    public void breakpointChanged(final IBreakpoint breakpoint, final IMarkerDelta delta) {
        if (supportsBreakpoint(breakpoint)) {
            try {
                if (breakpoint.isEnabled()) {
                    breakpointAdded(breakpoint);
                } else {
                    breakpointRemoved(breakpoint, null);
                }
            } catch (final CoreException e) {
            }
        }
    }

    @Override
    public boolean supportsBreakpoint(final IBreakpoint breakpoint) {
        return breakpoint.getModelIdentifier().equals(RobotDebugElement.DEBUG_MODEL_ID);
    }

    @Override
    public boolean supportsStorageRetrieval() {
        return false;
    }

    @Override
    public IMemoryBlock getMemoryBlock(final long startAddress, final long length) {
        return null;
    }

    /**
     * Returns the current stack frames in the target.
     * 
     * @return the current stack frames in the target
     */
    protected IStackFrame[] getStackFrames() {
        return robotDebugStackFrameManager.getStackFrames(currentKeywordDebugContextMap);
    }

    public void setHasStackFramesCreated(final boolean hasStackFramesCreated) {
        robotDebugStackFrameManager.setHasStackFramesCreated(hasStackFramesCreated);
    }
    
    public void clearStackFrames() {
        robotDebugStackFrameManager.setStackFrames(null);
    }

    /**
     * Set the thread's breakpoint
     * 
     * @param breakpoint
     */
    public void breakpointHit(final IBreakpoint breakpoint) {
        if (breakpoint instanceof ILineBreakpoint) {
            getThread().setSuspendedAt(breakpoint);
        }
    }

    public void sendChangeRequest(final String expression, final String variableName, final RobotDebugVariable parent) {
        userController.changeVariable(expression, variableName, parent);
    }
    
    public boolean hasStepOver() {
        return getThread().isSteppingOver() && currentStepOverLevel <= currentKeywordDebugContextMap.size();
    }
    
    public boolean hasStepReturn() {
        return getThread().isSteppingReturn() && currentStepReturnLevel <= currentKeywordDebugContextMap.size() + 1;
    }

    public RobotThread getRobotThread() {
        return getThread();
    }

    public Map<String, KeywordContext> getCurrentKeywordsContextMap() {
        return currentKeywordDebugContextMap;
    }

    public KeywordContext getLastKeywordFromCurrentContextMap() {
        if(currentKeywordDebugContextMap.size() > 0) {
            return Iterables.getLast(currentKeywordDebugContextMap.values());
        }
        return new KeywordContext();
    }
    
    public RobotDebugVariablesManager getRobotVariablesManager() {
        return robotVariablesManager;
    }

    public RobotDebugValueManager getRobotDebugValueManager() {
        return robotDebugValueManager;
    }
}
