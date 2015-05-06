package org.robotframework.ide.eclipse.main.plugin.debug.model;

import java.util.Map;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IRegisterGroup;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IVariable;

/**
 * @author mmarzec
 */
public class RobotStackFrame extends RobotDebugElement implements IStackFrame {

    private RobotThread thread;

    private IVariable[] variables;

    private String name;

    private String fileName;

    private int lineNumber;

    private int id;

    /**
     * Constructs a stack frame in the given thread with the given
     * frame data.
     * 
     * @param thread
     * @param keywordName
     *            frame name
     * @param fileName
     * @param vars
     * @param id
     *            stack frame id (0 is the bottom of the stack)
     */
    public RobotStackFrame(RobotThread thread, String fileName, String keywordName, int lineNumber,
            Map<String, String> vars, int id) {
        super((RobotDebugTarget) thread.getDebugTarget());
        this.id = id;
        this.thread = thread;
        // TODO: take resource file from keyword name, set here to automatically show it in editor
        this.fileName = fileName;
        this.lineNumber = lineNumber;
        name = keywordName + " (line:" + lineNumber + ")";
        initVariables(keywordName, vars);
    }

    /**
     * Initializes this frame based on its data
     * 
     * @param data
     */
    private void initVariables(String keywordName, Map<String, String> vars) {

        variables = new IVariable[vars.size()];
        int i = 0;
        for (String key : vars.keySet()) {
            variables[i] = new RobotDebugVariable(this, key, vars.get(key));
            i++;
        }

    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.IStackFrame#getThread()
     */
    public IThread getThread() {
        return thread;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.IStackFrame#getVariables()
     */
    public IVariable[] getVariables() throws DebugException {
        return variables;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.IStackFrame#hasVariables()
     */
    public boolean hasVariables() throws DebugException {
        return variables.length > 0;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.IStackFrame#getLineNumber()
     */
    public int getLineNumber() throws DebugException {
        return lineNumber;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.IStackFrame#getCharStart()
     */
    public int getCharStart() throws DebugException {
        return -1;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.IStackFrame#getCharEnd()
     */
    public int getCharEnd() throws DebugException {
        return -1;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.IStackFrame#getName()
     */
    public String getName() throws DebugException {
        return name;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.IStackFrame#getRegisterGroups()
     */
    public IRegisterGroup[] getRegisterGroups() throws DebugException {
        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.IStackFrame#hasRegisterGroups()
     */
    public boolean hasRegisterGroups() throws DebugException {
        return false;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.IStep#canStepInto()
     */
    public boolean canStepInto() {
        return getThread().canStepInto();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.IStep#canStepOver()
     */
    public boolean canStepOver() {
        return getThread().canStepOver();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.IStep#canStepReturn()
     */
    public boolean canStepReturn() {
        return getThread().canStepReturn();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.IStep#isStepping()
     */
    public boolean isStepping() {
        return getThread().isStepping();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.IStep#stepInto()
     */
    public void stepInto() throws DebugException {
        getThread().stepInto();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.IStep#stepOver()
     */
    public void stepOver() throws DebugException {
        getThread().stepOver();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.IStep#stepReturn()
     */
    public void stepReturn() throws DebugException {
        getThread().stepReturn();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.ISuspendResume#canResume()
     */
    public boolean canResume() {
        return getThread().canResume();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.ISuspendResume#canSuspend()
     */
    public boolean canSuspend() {
        return getThread().canSuspend();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.ISuspendResume#isSuspended()
     */
    public boolean isSuspended() {
        return getThread().isSuspended();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.ISuspendResume#resume()
     */
    public void resume() throws DebugException {
        getThread().resume();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.ISuspendResume#suspend()
     */
    public void suspend() throws DebugException {
        getThread().suspend();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.ITerminate#canTerminate()
     */
    public boolean canTerminate() {
        return getThread().canTerminate();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.ITerminate#isTerminated()
     */
    public boolean isTerminated() {
        return getThread().isTerminated();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.ITerminate#terminate()
     */
    public void terminate() throws DebugException {
        getThread().terminate();
    }

    /**
     * Returns the name of the source file this stack frame is associated
     * with.
     * 
     * @return the name of the source file this stack frame is associated
     *         with
     */
    public String getSourceName() {
        return fileName;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (obj instanceof RobotStackFrame) {
            RobotStackFrame sf = (RobotStackFrame) obj;
            try {
                return sf.getSourceName().equals(getSourceName()) && sf.getLineNumber() == getLineNumber()
                        && sf.id == id;
            } catch (DebugException e) {
            }
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return getSourceName().hashCode() + id;
    }

    /**
     * Returns this stack frame's unique identifier within its thread
     * 
     * @return this stack frame's unique identifier within its thread
     */
    protected int getIdentifier() {
        return id;
    }
}
