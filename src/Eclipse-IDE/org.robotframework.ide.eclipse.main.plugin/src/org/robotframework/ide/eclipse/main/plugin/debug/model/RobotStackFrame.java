/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.debug.model;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;
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

    private final RobotThread thread;

    private IVariable[] variables;

    private String name;

    private String fileName;

    private int lineNumber;

    /**
     * Level in stack.
     */
    private int id;

    /**
     * Constructs a stack frame in the given thread with the given
     * frame data.
     * 
     * @param thread
     * @param fileName
     * @param keywordName
     * @param lineNumber
     * @param vars
     * @param id
     *            stack frame id (1 is the bottom of the stack)
     */
    public RobotStackFrame(final RobotThread thread, final String fileName, final String keywordName, final int lineNumber,
            final Map<String, Object> vars, final int id) {
        super((RobotDebugTarget) thread.getDebugTarget());
        this.id = id;
        this.thread = thread;
        this.fileName = fileName;
        this.lineNumber = lineNumber;
        name = keywordName + " [line:" + lineNumber + "]";
        initVariables(vars);
    }

    public void initVariables(final Map<String, Object> vars) {

        variables = ((RobotDebugTarget) thread.getDebugTarget()).getRobotVariablesManager().extractRobotDebugVariables(
                id, vars);
    }

    @Override
    public IThread getThread() {
        return thread;
    }

    @Override
    public IVariable[] getVariables() throws DebugException {
        return variables;
    }

    // gets nested variables too
    public List<IVariable> getAllVariables() throws DebugException {
        final List<IVariable> vars = newArrayList(variables);
        for (final IVariable var : variables) {
            final RobotDebugVariable robotVar = (RobotDebugVariable) var;
            final RobotDebugValue value = (RobotDebugValue) robotVar.getValue();

            vars.addAll(newArrayList(value.getVariables()));
        }
        
        return vars;
    }

    @Override
    public boolean hasVariables() throws DebugException {
        return variables.length > 0;
    }

    @Override
    public int getLineNumber() throws DebugException {
        return lineNumber;
    }

    @Override
    public int getCharStart() throws DebugException {
        return -1;
    }

    @Override
    public int getCharEnd() throws DebugException {
        return -1;
    }

    @Override
    public String getName() throws DebugException {
        return name;
    }

    @Override
    public IRegisterGroup[] getRegisterGroups() throws DebugException {
        return null;
    }

    @Override
    public boolean hasRegisterGroups() throws DebugException {
        return false;
    }

    @Override
    public boolean canStepInto() {
        return getThread().canStepInto();
    }

    @Override
    public boolean canStepOver() {
        return getThread().canStepOver();
    }

    @Override
    public boolean canStepReturn() {
        return getThread().canStepReturn();
    }

    @Override
    public boolean isStepping() {
        return getThread().isStepping();
    }

    @Override
    public void stepInto() throws DebugException {
        getThread().stepInto();
    }

    @Override
    public void stepOver() throws DebugException {
        getThread().stepOver();
    }

    @Override
    public void stepReturn() throws DebugException {
        getThread().stepReturn();
    }

    @Override
    public boolean canResume() {
        return getThread().canResume();
    }

    @Override
    public boolean canSuspend() {
        return getThread().canSuspend();
    }

    @Override
    public boolean isSuspended() {
        return getThread().isSuspended();
    }

    @Override
    public void resume() throws DebugException {
        getThread().resume();
    }

    @Override
    public void suspend() throws DebugException {
        getThread().suspend();
    }

    @Override
    public boolean canTerminate() {
        return getThread().canTerminate();
    }

    @Override
    public boolean isTerminated() {
        return getThread().isTerminated();
    }

    @Override
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

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof RobotStackFrame) {
            final RobotStackFrame sf = (RobotStackFrame) obj;
            try {
                return sf.getSourceName().equals(getSourceName()) && sf.getLineNumber() == getLineNumber()
                        && sf.id == id;
            } catch (final DebugException e) {
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getSourceName().hashCode() + id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(final String fileName) {
        this.fileName = fileName;
    }

    public int getId() {
        return id;
    }

    public void setId(final int id) {
        this.id = id;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setLineNumber(final int lineNumber) {
        this.lineNumber = lineNumber;
    }

}
