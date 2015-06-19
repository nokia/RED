package org.robotframework.ide.eclipse.main.plugin.debug.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResource;
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
import org.robotframework.ide.eclipse.main.plugin.debug.RobotDebugEventDispatcher;
import org.robotframework.ide.eclipse.main.plugin.debug.RobotPartListener;
import org.robotframework.ide.eclipse.main.plugin.debug.utils.KeywordContext;
import org.robotframework.ide.eclipse.main.plugin.debug.utils.RobotDebugValueManager;
import org.robotframework.ide.eclipse.main.plugin.debug.utils.RobotDebugVariablesManager;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotEventBroker;

/**
 * @author mmarzec
 */
public class RobotDebugTarget extends RobotDebugElement implements IDebugTarget {

    // associated system process (Robot)
    private final IProcess process;

    // containing launch object
    private final ILaunch launch;

    // program name
    private String name;

    // socket to communicate with Agent
    private Socket eventSocket;

    private PrintWriter eventWriter;

    private BufferedReader eventReader;

    private ServerSocket serverSocket;

    // suspend state
    private boolean isSuspended = false;

    private boolean hasStackFramesCreated;

    // threads
    private final RobotThread thread;

    private final IThread[] threads;

    private final Map<String, KeywordContext> currentFrames;

    private IStackFrame[] stackFrames;
    
    private int currentStepOverLevel = 0;
    
    private int currentStepReturnLevel = 0;

    private final RobotPartListener partListener;

    private final RobotEventBroker robotEventBroker;
    
    private final RobotDebugVariablesManager robotVariablesManager;
    
    private final RobotDebugValueManager robotDebugValueManager;

    public RobotDebugTarget(final ILaunch launch, final IProcess process, final int port,
            final List<IResource> suiteResources, final RobotPartListener partListener,
            final RobotEventBroker robotEventBroker) throws CoreException {
        super(null);
        target = this;
        this.launch = launch;
        this.process = process;
        this.partListener = partListener;
        this.robotEventBroker = robotEventBroker;
        currentFrames = new LinkedHashMap<>();
        robotVariablesManager = new RobotDebugVariablesManager(this);
        robotDebugValueManager = new RobotDebugValueManager();

        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setReuseAddress(true);
            serverSocket.setSoTimeout(10000);
            eventSocket = serverSocket.accept();
            eventReader = new BufferedReader(new InputStreamReader(eventSocket.getInputStream()));
            eventWriter = new PrintWriter(eventSocket.getOutputStream(), true);
        } catch (final IOException e) {
            e.printStackTrace();
        }

        thread = new RobotThread(this);
        threads = new IThread[] { thread };

        final RobotDebugEventDispatcher eventDispatcher = new RobotDebugEventDispatcher(this, suiteResources,
                robotEventBroker);
        eventDispatcher.schedule();

        DebugPlugin.getDefault().getBreakpointManager().addBreakpointListener(this);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.IDebugTarget#getProcess()
     */
    @Override
    public IProcess getProcess() {
        return process;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.IDebugTarget#getThreads()
     */
    @Override
    public IThread[] getThreads() throws DebugException {
        return threads;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.IDebugTarget#hasThreads()
     */
    @Override
    public boolean hasThreads() throws DebugException {
        return true;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.IDebugTarget#getName()
     */
    @Override
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
    @Override
    public IDebugTarget getDebugTarget() {
        return this;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.IDebugElement#getLaunch()
     */
    @Override
    public ILaunch getLaunch() {
        return launch;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.IDisconnect#canDisconnect()
     */
    @Override
    public boolean canDisconnect() {
        return false;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.IDisconnect#isDisconnected()
     */
    @Override
    public boolean isDisconnected() {
        return false;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.IDisconnect#disconnect()
     */
    @Override
    public void disconnect() throws DebugException {

    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.ITerminate#canTerminate()
     */
    @Override
    public boolean canTerminate() {
        return getProcess().canTerminate();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.ITerminate#isTerminated()
     */
    @Override
    public boolean isTerminated() {
        return getProcess().isTerminated();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.ITerminate#terminate()
     */
    @Override
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
    @Override
    public boolean canSuspend() {
        return !isTerminated() && !isSuspended();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.ISuspendResume#isSuspended()
     */
    @Override
    public boolean isSuspended() {
        return isSuspended;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.ISuspendResume#suspend()
     */
    @Override
    public void suspend() throws DebugException {

    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.ISuspendResume#canResume()
     */
    @Override
    public boolean canResume() {
        return !isTerminated() && isSuspended();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.ISuspendResume#resume()
     */
    @Override
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
    public void resumed(final int detail) {
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
        installDeferredBreakpoints();
    }

    /**
     * Called when this debug target terminates.
     */
    public void terminated() {
        isSuspended = false;
        DebugPlugin.getDefault().getBreakpointManager().removeBreakpointListener(this);
        fireTerminateEvent();
       
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.IBreakpointListener#breakpointAdded(org.eclipse.debug.core.model.
     * IBreakpoint)
     */
    @Override
    public void breakpointAdded(final IBreakpoint breakpoint) {

    }

    /*
     * (non-Javadoc)
     * @see
     * org.eclipse.debug.core.IBreakpointListener#breakpointRemoved(org.eclipse.debug.core.model
     * .IBreakpoint, org.eclipse.core.resources.IMarkerDelta)
     */
    @Override
    public void breakpointRemoved(final IBreakpoint breakpoint, final IMarkerDelta delta) {
        if (supportsBreakpoint(breakpoint)) {

        }
    }

    /*
     * (non-Javadoc)
     * @see
     * org.eclipse.debug.core.IBreakpointListener#breakpointChanged(org.eclipse.debug.core.model
     * .IBreakpoint, org.eclipse.core.resources.IMarkerDelta)
     */
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

    /*
     * (non-Javadoc)
     * @see
     * org.eclipse.debug.core.model.IDebugTarget#supportsBreakpoint(org.eclipse.debug.core.model
     * .IBreakpoint)
     */
    @Override
    public boolean supportsBreakpoint(final IBreakpoint breakpoint) {
        if (breakpoint.getModelIdentifier().equals(RobotDebugElement.DEBUG_MODEL_ID)) {
            return true;
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.IMemoryBlockRetrieval#supportsStorageRetrieval()
     */
    @Override
    public boolean supportsStorageRetrieval() {
        return false;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.IMemoryBlockRetrieval#getMemoryBlock(long, long)
     */
    @Override
    public IMemoryBlock getMemoryBlock(final long startAddress, final long length) throws DebugException {
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

        if (!hasStackFramesCreated) {

            int previousStackFramesSize = 0;
            if (stackFrames != null) {
                previousStackFramesSize = stackFrames.length;
            }

            final IStackFrame[] newStackFrames = new IStackFrame[currentFrames.size()];
            int id = 1;
            for (final String key : currentFrames.keySet()) {
                final KeywordContext keywordContext = currentFrames.get(key);
                //only the highest level of StackFrames is created, lower levels are copied from previous StackFrames
                if (id >= currentFrames.size() || previousStackFramesSize == 0) {
                    newStackFrames[currentFrames.size() - id] = new RobotStackFrame(thread,
                            keywordContext.getFileName(), key, keywordContext.getLineNumber(),
                            keywordContext.getVariables(), id);
                } else {
                    newStackFrames[currentFrames.size() - id] = stackFrames[previousStackFramesSize - id];
                }
                id++;
            }
            stackFrames = newStackFrames;
            hasStackFramesCreated = true;
        }

        return stackFrames;
    }

    public void setHasStackFramesCreated(final boolean hasCreated) {
        hasStackFramesCreated = hasCreated;
    }
    
    public void clearStackFrames() {
        stackFrames = null;
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

    /**
     * Sends a message to the TestRunnerAgent
     * 
     * @param event
     */
    public void sendEventToAgent(final String event) {

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
    public void sendChangeVariableRequest(final String variable, final String value) {

        sendEventToAgent("{\"" + variable + "\":[\"" + value + "\"]}");
    }
    
    /**
     * Sends a message with change variable request to the TestRunnerAgent
     * 
     * @param variable
     * @param childList
     * @param value
     */
    public void sendChangeCollectionRequest(final String variable, final List<String> childList, final String value) {
        
        final StringBuilder requestJson = new StringBuilder();
        requestJson.append("{\"" + variable + "\":[");
        for (int i = 0; i < childList.size(); i++) {
            requestJson.append("\"" + childList.get(i) + "\",");
        }
        requestJson.append("\"" + value + "\"]}");
        sendEventToAgent(requestJson.toString());
    }
    
    public void sendChangeRequest(final String expression, final String variableName, final RobotDebugVariable parent) {
        
        if (parent != null) {
            final LinkedList<String> childNameList = new LinkedList<String>();
            final String root = robotVariablesManager.extractVariableRootAndChilds(parent, childNameList, variableName);
            sendChangeCollectionRequest(root, childNameList, expression);
        } else {
            sendChangeVariableRequest(name, expression);
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
    
    public RobotDebugVariablesManager getRobotVariablesManager() {
        return robotVariablesManager;
    }

    public RobotDebugValueManager getRobotDebugValueManager() {
        return robotDebugValueManager;
    }

}
