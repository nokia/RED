package org.robotframework.ide.eclipse.main.plugin.debug.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
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
import org.robotframework.ide.eclipse.main.plugin.debug.KeywordContext;
import org.robotframework.ide.eclipse.main.plugin.debug.RobotDebugEventDispatcher;
import org.robotframework.ide.eclipse.main.plugin.debug.RobotPartListener;
import org.robotframework.ide.eclipse.main.plugin.debug.RobotVariablesManager;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotEventBroker;

/**
 * @author mmarzec
 */
public class RobotDebugTarget extends RobotDebugElement implements IDebugTarget {

    // associated system process (Robot)
    private IProcess process;

    // containing launch object
    private ILaunch launch;

    // program name
    private String name;

    // socket to communicate with Agent
    private Socket eventSocket;

    private PrintWriter eventWriter;

    private BufferedReader eventReader;

    private ServerSocket serverSocket;

    // suspend state
    private boolean isSuspended = false;

    // terminated state
    private boolean isTerminated = false;

    // threads
    private RobotThread thread;

    private IThread[] threads;

    private Map<String, KeywordContext> currentFrames;

    private IStackFrame[] stackFrames;

    private int currentStepOverLevel = 0;
    
    private int currentStepReturnLevel = 0;

    private RobotPartListener partListener;

    private RobotEventBroker robotEventBroker;
    
    private RobotVariablesManager robotVariablesManager;

    public RobotDebugTarget(ILaunch launch, IProcess process, int port, IFile executedFile,
            RobotPartListener partListener, RobotEventBroker robotEventBroker) throws CoreException {
        super(null);
        target = this;
        this.launch = launch;
        this.process = process;
        this.partListener = partListener;
        this.robotEventBroker = robotEventBroker;
        currentFrames = new LinkedHashMap<>();
        robotVariablesManager = new RobotVariablesManager(this);

        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setReuseAddress(true);
            eventSocket = serverSocket.accept();
            eventReader = new BufferedReader(new InputStreamReader(eventSocket.getInputStream()));
            eventWriter = new PrintWriter(eventSocket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        thread = new RobotThread(this);
        threads = new IThread[] { thread };

        RobotDebugEventDispatcher eventDispatcher = new RobotDebugEventDispatcher(this, executedFile,
                robotEventBroker);
        eventDispatcher.schedule();

        DebugPlugin.getDefault().getBreakpointManager().addBreakpointListener(this);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.IDebugTarget#getProcess()
     */
    public IProcess getProcess() {
        return process;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.IDebugTarget#getThreads()
     */
    public IThread[] getThreads() throws DebugException {
        return threads;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.IDebugTarget#hasThreads()
     */
    public boolean hasThreads() throws DebugException {
        return true;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.IDebugTarget#getName()
     */
    public String getName() throws DebugException {
        if (name == null) {
            name = "Robot Test";

        }
        return name;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.IDebugElement#getDebugTarget()
     */
    public IDebugTarget getDebugTarget() {
        return this;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.IDebugElement#getLaunch()
     */
    public ILaunch getLaunch() {
        return launch;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.IDisconnect#canDisconnect()
     */
    public boolean canDisconnect() {
        return false;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.IDisconnect#isDisconnected()
     */
    public boolean isDisconnected() {
        return false;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.IDisconnect#disconnect()
     */
    public void disconnect() throws DebugException {

    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.ITerminate#canTerminate()
     */
    public boolean canTerminate() {
        return getProcess().canTerminate();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.ITerminate#isTerminated()
     */
    public boolean isTerminated() {
        return getProcess().isTerminated();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.ITerminate#terminate()
     */
    public void terminate() throws DebugException {
        if (eventSocket != null) {
            sendEventToAgent("interrupt");
        }
        terminated();

        robotEventBroker.sendClearAllEventToTextEditor();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.ISuspendResume#canSuspend()
     */
    public boolean canSuspend() {
        return !isTerminated() && !isSuspended();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.ISuspendResume#isSuspended()
     */
    public boolean isSuspended() {
        return isSuspended;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.ISuspendResume#suspend()
     */
    public void suspend() throws DebugException {

    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.ISuspendResume#canResume()
     */
    public boolean canResume() {
        return !isTerminated() && isSuspended();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.ISuspendResume#resume()
     */
    public void resume() throws DebugException {
        thread.setStepping(false);
        sendEventToAgent("resume");
        resumed(DebugEvent.CLIENT_REQUEST);

        robotEventBroker.sendClearAllEventToTextEditor();
    }

    protected void step() {
        thread.setStepping(true);
        sendEventToAgent("resume");
        resumed(DebugEvent.CLIENT_REQUEST);
    }

    protected void stepOver() {
        currentStepOverLevel = currentFrames.size();
        step();
    }
    
    protected void stepReturn() {
        currentStepReturnLevel = currentFrames.size();
        step();
    }

    /**
     * Notification the target has resumed for the given reason
     * 
     * @param detail
     *            reason for the resume
     */
    public void resumed(int detail) {
        isSuspended = false;
        thread.fireResumeEvent(detail);
    }

    /**
     * Notification the target has suspended for the given reason
     * 
     * @param detail
     *            reason for the suspend
     */
    public void suspended(int detail) {
        isSuspended = true;
        thread.fireSuspendEvent(detail);
    }

    /**
     * Notification we have connected to the Agent and it has started.
     */
    public void started() {
        fireCreationEvent();
        installDeferredBreakpoints();
    }

    /**
     * Called when this debug target terminates.
     */
    public void terminated() {
        isTerminated = true;
        isSuspended = false;
        DebugPlugin.getDefault().getBreakpointManager().removeBreakpointListener(this);
        fireTerminateEvent();
       
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.IBreakpointListener#breakpointAdded(org.eclipse.debug.core.model.
     * IBreakpoint)
     */
    public void breakpointAdded(IBreakpoint breakpoint) {

    }

    /*
     * (non-Javadoc)
     * @see
     * org.eclipse.debug.core.IBreakpointListener#breakpointRemoved(org.eclipse.debug.core.model
     * .IBreakpoint, org.eclipse.core.resources.IMarkerDelta)
     */
    public void breakpointRemoved(IBreakpoint breakpoint, IMarkerDelta delta) {
        if (supportsBreakpoint(breakpoint)) {

        }
    }

    /*
     * (non-Javadoc)
     * @see
     * org.eclipse.debug.core.IBreakpointListener#breakpointChanged(org.eclipse.debug.core.model
     * .IBreakpoint, org.eclipse.core.resources.IMarkerDelta)
     */
    public void breakpointChanged(IBreakpoint breakpoint, IMarkerDelta delta) {
        if (supportsBreakpoint(breakpoint)) {
            try {
                if (breakpoint.isEnabled()) {
                    breakpointAdded(breakpoint);
                } else {
                    breakpointRemoved(breakpoint, null);
                }
            } catch (CoreException e) {
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see
     * org.eclipse.debug.core.model.IDebugTarget#supportsBreakpoint(org.eclipse.debug.core.model
     * .IBreakpoint)
     */
    public boolean supportsBreakpoint(IBreakpoint breakpoint) {
        if (breakpoint.getModelIdentifier().equals(RobotDebugElement.DEBUG_MODEL_ID)) {
            return true;
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.IMemoryBlockRetrieval#supportsStorageRetrieval()
     */
    public boolean supportsStorageRetrieval() {
        return false;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.IMemoryBlockRetrieval#getMemoryBlock(long, long)
     */
    public IMemoryBlock getMemoryBlock(long startAddress, long length) throws DebugException {
        return null;
    }

    /**
     * Install breakpoints that are already registered with the breakpoint
     * manager.
     */
    private void installDeferredBreakpoints() {
        // IBreakpoint[] breakpoints = DebugPlugin.getDefault()
        // .getBreakpointManager()
        // .getBreakpoints(RobotDebugElement.DEBUG_MODEL_ID);
        // for (int i = 0; i < breakpoints.length; i++) {
        // breakpointAdded(breakpoints[i]);
        // }
    }

    /**
     * Returns the current stack frames in the target.
     * 
     * @return the current stack frames in the target
     */
    protected IStackFrame[] getStackFrames() {

        if (stackFrames == null) {
            stackFrames = new IStackFrame[currentFrames.size()];
            int i = 1;
            for (String key : currentFrames.keySet()) {
                KeywordContext keywordContext = currentFrames.get(key);

                stackFrames[currentFrames.size() - i] = new RobotStackFrame(thread, keywordContext.getFileName(), key,
                        keywordContext.getLineNumber(), keywordContext.getVariables(), i);
                i++;
            }
        }

        return stackFrames;
    }

    public void clearStackFrames() {
        stackFrames = null;
    }

    /**
     * Set the thread's breakpoint
     * 
     * @param breakpoint
     */
    public void breakpointHit(IBreakpoint breakpoint) {

        if (breakpoint instanceof ILineBreakpoint) {
            thread.setBreakpoints(new IBreakpoint[] { breakpoint });
        }
    }

    /**
     * Sends a message to the TestRunnerAgent
     * 
     * @param event
     */
    public void sendEventToAgent(String event) {

        synchronized (eventSocket) {
            eventWriter.print(event);
            eventWriter.flush();
        }
    }

    /**
     * Sends a message with change variable request to the TestRunnerAgent
     * 
     * @param variable
     * @param value
     */
    public void sendChangeVariableRequest(String variable, String value) {

        synchronized (eventSocket) {
            eventWriter.print("{\"" + variable + "\":[\"" + value + "\"]}");
            eventWriter.flush();
        }
    }
    
    /**
     * Sends a message with change variable request to the TestRunnerAgent
     * 
     * @param variable
     * @param childList
     * @param value
     */
    public void sendChangeCollectionRequest(String variable, List<String> childList, String value) {
        StringBuilder requestJson = new StringBuilder();
        requestJson.append("{\"" + variable + "\":[");
        for (int i = 0; i < childList.size(); i++) {
            requestJson.append("\"" + childList.get(i) + "\",");
        }
        requestJson.append("\"" + value + "\"]}");
        synchronized (eventSocket) {
            eventWriter.print(requestJson.toString());
            eventWriter.flush();
        }
    }

    public boolean hasStepOver() {

        if (thread.isSteppingOver() && currentStepOverLevel <= currentFrames.size()) {
            return true;
        }

        return false;
    }
    
    public boolean hasStepReturn() {

        if (thread.isSteppingReturn() && currentStepReturnLevel <= currentFrames.size()+1) {
            return true;
        }

        return false;
    }

    public RobotPartListener getPartListener() {
        return partListener;
    }

    public BufferedReader getEventReader() {
        return eventReader;
    }

    public RobotThread getRobotThread() {
        return thread;
    }

    public Map<String, KeywordContext> getCurrentFrames() {
        return currentFrames;
    }

    public KeywordContext getLastKeywordFromCurrentFrames() {
        if(currentFrames.size() > 0) {
            return (KeywordContext) currentFrames.values().toArray()[currentFrames.size() - 1];
        }
        return new KeywordContext();
    }
    
    public RobotVariablesManager getRobotVariablesManager() {
        return robotVariablesManager;
    }
}
