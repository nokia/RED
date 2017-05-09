/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.debug.model;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
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
import org.rf.ide.core.execution.agent.RobotAgentEventListener.RobotAgentEventsListenerException;
import org.rf.ide.core.execution.server.AgentClient;
import org.rf.ide.core.execution.server.response.ChangeVariable;
import org.rf.ide.core.execution.server.response.InterruptExecution;
import org.rf.ide.core.execution.server.response.ResumeExecution;
import org.rf.ide.core.execution.server.response.ServerResponse.ResponseException;
import org.robotframework.ide.eclipse.main.plugin.debug.utils.KeywordContext;
import org.robotframework.ide.eclipse.main.plugin.debug.utils.RobotDebugStackFrameManager;
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

    private final Map<String, KeywordContext> currentKeywordsDebugContextMap;

    private int currentStepOverLevel;
    
    private int currentStepReturnLevel;

    private final RobotDebugVariablesManager robotVariablesManager;
    
    private RobotDebugStackFrameManager robotDebugStackFrameManager;
    
    private AgentClient client;

    public RobotDebugTarget(final String name, final ILaunch launch) {
        super(null);
        this.name = name;
        this.launch = launch;
        this.currentKeywordsDebugContextMap = new LinkedHashMap<>();
        this.robotVariablesManager = new RobotDebugVariablesManager(this);

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
        this.client = client;
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
        if (client != null) {
            try {
                client.send(new InterruptExecution());
            } catch (ResponseException | IOException e) {
                throw new RobotAgentEventsListenerException("Unable to send response to client", e);
            }
        }
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

        try {
            client.send(new ResumeExecution());
        } catch (ResponseException | IOException e) {
            throw new RobotAgentEventsListenerException("Unable to send response to client", e);
        }
        resumed(DebugEvent.CLIENT_REQUEST);
    }

    protected void step() {
        getThread().setStepping(true);

        try {
            client.send(new ResumeExecution());
        } catch (ResponseException | IOException e) {
            throw new RobotAgentEventsListenerException("Unable to send response to client", e);
        }
        resumed(DebugEvent.CLIENT_REQUEST);
    }

    protected void stepOver() {
        currentStepOverLevel = currentKeywordsDebugContextMap.size();
        step();
    }
    
    protected void stepReturn() {
        currentStepReturnLevel = currentKeywordsDebugContextMap.size();
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
       
        final IProcess process = getProcess();
        if (process != null) {
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
        return robotDebugStackFrameManager.getStackFrames(currentKeywordsDebugContextMap);
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

    void sendChangeRequest(final String variableName, final List<String> values) {
        try {
            client.send(new ChangeVariable(variableName, values));
        } catch (ResponseException | IOException e) {
            throw new RobotAgentEventsListenerException("Unable to send response to client", e);
        }
    }
    
    public boolean hasStepOver() {
        return getThread().isSteppingOver() && currentStepOverLevel <= currentKeywordsDebugContextMap.size();
    }
    
    public boolean hasStepReturn() {
        return getThread().isSteppingReturn() && currentStepReturnLevel <= currentKeywordsDebugContextMap.size() + 1;
    }

    public RobotThread getRobotThread() {
        return getThread();
    }

    public Map<String, KeywordContext> getCurrentKeywordsContext() {
        return currentKeywordsDebugContextMap;
    }

    public KeywordContext getLastKeywordFromCurrentContext() {
        if(currentKeywordsDebugContextMap.size() > 0) {
            return Iterables.getLast(currentKeywordsDebugContextMap.values());
        }
        return new KeywordContext();
    }
    
    public RobotDebugVariablesManager getRobotVariablesManager() {
        return robotVariablesManager;
    }
}
