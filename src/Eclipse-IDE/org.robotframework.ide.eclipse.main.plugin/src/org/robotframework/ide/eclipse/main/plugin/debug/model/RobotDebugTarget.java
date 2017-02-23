/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.debug.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
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
import org.rf.ide.core.execution.RobotAgentEventListener;
import org.rf.ide.core.execution.context.RobotDebugExecutionContext;
import org.rf.ide.core.execution.server.AgentClient;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.debug.DebugExecutionEventsListener;
import org.robotframework.ide.eclipse.main.plugin.debug.ExecutionTrackerForExecutionView;
import org.robotframework.ide.eclipse.main.plugin.debug.MessagesTrackerForLogView;
import org.robotframework.ide.eclipse.main.plugin.debug.RobotAgentEventsJob;
import org.robotframework.ide.eclipse.main.plugin.debug.utils.DebugSocketManager;
import org.robotframework.ide.eclipse.main.plugin.debug.utils.KeywordContext;
import org.robotframework.ide.eclipse.main.plugin.debug.utils.RobotDebugStackFrameManager;
import org.robotframework.ide.eclipse.main.plugin.debug.utils.RobotDebugValueManager;
import org.robotframework.ide.eclipse.main.plugin.debug.utils.RobotDebugVariablesManager;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotEventBroker;

import com.google.common.collect.Iterables;

/**
 * @author mmarzec
 */
@SuppressWarnings({ "PMD.TooManyFields", "PMD.TooManyMethods", "PMD.GodClass" })
public class RobotDebugTarget extends RobotDebugElement implements IDebugTarget {

    // associated system process (Robot)
    private final IProcess process;

    // containing launch object
    private final ILaunch launch;

    // program name
    private String name;
    
    private ServerSocket serverSocket;

    // socket to communicate with Agent
    private Socket eventSocket;

    private PrintWriter eventWriter;

    private BufferedReader eventReader;

    // suspend state
    private boolean isSuspended;

    // threads
    private RobotThread thread;

    private IThread[] threads;

    private final Map<String, KeywordContext> currentKeywordDebugContextMap;

    private int currentStepOverLevel;
    
    private int currentStepReturnLevel;

    private final RobotDebugVariablesManager robotVariablesManager;
    
    private final RobotDebugValueManager robotDebugValueManager;
    
    private RobotDebugStackFrameManager robotDebugStackFrameManager;
    
    private UserController userController;

    public RobotDebugTarget(final ILaunch launch, final IProcess process) {
        super(null);
        this.launch = launch;
        this.process = process;
        this.currentKeywordDebugContextMap = new LinkedHashMap<>();
        this.robotVariablesManager = new RobotDebugVariablesManager(this);
        this.robotDebugValueManager = new RobotDebugValueManager();

        this.thread = null;
        this.threads = null;

        this.isSuspended = false;
        this.currentStepOverLevel = 0;
        this.currentStepReturnLevel = 0;

        this.robotDebugStackFrameManager = null;
    }

    public void connect(final List<IResource> suiteResources, final RobotEventBroker robotEventBroker,
            final DebugSocketManager socketManager) throws CoreException {
        int retryCounter = 0;
        try {  //wait for TestRunnerAgent
            while (socketManager.getEventSocket() == null ? true : (eventReader = new BufferedReader(
                    new InputStreamReader(socketManager.getEventSocket().getInputStream()))).readLine() == null) {
                try {
                    Thread.sleep(DebugSocketManager.WAIT_FOR_AGENT_TIME);
                } catch (final InterruptedException e) {
                    e.printStackTrace();
                }
                retryCounter++;
                if(process.isTerminated()) {
                    throw new CoreException(Status.CANCEL_STATUS);
                }
                if(retryCounter > socketManager.getRetryCounterMaxValue()) {
                    process.terminate();
                    throw new CoreException(Status.CANCEL_STATUS);
                }
            }
            serverSocket = socketManager.getServerSocket();
            eventSocket = socketManager.getEventSocket();
            eventWriter = new PrintWriter(eventSocket.getOutputStream(), true);
        } catch (final IOException e) {
            throw new CoreException(
                    new Status(IStatus.ERROR, RedPlugin.PLUGIN_ID, "Unable to connect with debug target", e));
        }

        thread = new RobotThread(this);
        threads = new IThread[] { thread };
        
        robotDebugStackFrameManager = new RobotDebugStackFrameManager(thread);

        final RobotDebugExecutionContext executionContext = new RobotDebugExecutionContext();
        final List<RobotAgentEventListener> listeners = new ArrayList<>();
        listeners.add(new DebugExecutionEventsListener(this, suiteResources, executionContext));
        listeners.add(new MessagesTrackerForLogView());
        listeners.add(new ExecutionTrackerForExecutionView(robotEventBroker));
        final AgentClient agentClient = new AgentClient(0, eventWriter);
        userController = new UserController(robotVariablesManager, agentClient);
        final RobotAgentEventsJob eventsJob = new RobotAgentEventsJob(eventReader, agentClient,
                listeners.toArray(new RobotAgentEventListener[0]));
        eventsJob.schedule();

        DebugPlugin.getDefault().getBreakpointManager().addBreakpointListener(this);
    }

    @Override
    public IProcess getProcess() {
        return process;
    }

    @Override
    public IThread[] getThreads() throws DebugException {
        return threads;
    }

    @Override
    public boolean hasThreads() throws DebugException {
        return true;
    }

    @Override
    public String getName() throws DebugException {
        if (name == null) {
            name = "Robot Test at " + eventSocket.getInetAddress().getHostAddress() + ":" + eventSocket.getLocalPort();
        }
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
    public void disconnect() throws DebugException {

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
        if (eventSocket != null) {
            userController.interrupt();
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
    public void suspend() throws DebugException {

    }

    @Override
    public boolean canResume() {
        return !isTerminated() && isSuspended();
    }

    @Override
    public void resume() throws DebugException {
        thread.setStepping(false);
        userController.resume();
        resumed(DebugEvent.CLIENT_REQUEST);
    }

    protected void step() {
        thread.setStepping(true);
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
        thread.fireResumeEvent(detail);
    }

    /**
     * Notification the target has suspended for the given reason
     * 
     * @param detail
     *            reason for the suspend
     */
    public void suspended(final int detail) {
        isSuspended = true;
        thread.fireSuspendEvent(detail);
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
       
        try {
            if (eventWriter != null) {
                eventWriter.close();
            }
            if (eventReader != null) {
                eventReader.close();
            }
            if (eventSocket != null) {
                eventSocket.close();
            }
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }

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
    public IMemoryBlock getMemoryBlock(final long startAddress, final long length) throws DebugException {
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
            thread.setBreakpoints(new IBreakpoint[] { breakpoint });
        }
    }

    public void sendChangeRequest(final String expression, final String variableName, final RobotDebugVariable parent) {
        userController.changeVariable(expression, variableName, parent);
    }
    
    public boolean hasStepOver() {
        return thread.isSteppingOver() && currentStepOverLevel <= currentKeywordDebugContextMap.size();
    }
    
    public boolean hasStepReturn() {
        return thread.isSteppingReturn() && currentStepReturnLevel <= currentKeywordDebugContextMap.size() + 1;
    }

    public BufferedReader getEventReader() {
        return eventReader;
    }

    public RobotThread getRobotThread() {
        return thread;
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
