/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.debug.model;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.debug.core.model.IRegisterGroup;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IVariable;
import org.robotframework.ide.eclipse.main.plugin.debug.utils.KeywordContext;

/**
 * @author mmarzec
 */
public class RobotStackFrame extends RobotDebugElement implements IStackFrame {

    private final RobotThread thread;

    private IVariable[] variables;

    private String name;

    private String fileName;

    private int lineNumber;

    private int stackLevel;

    /**
     * Constructs a stack frame in the given thread with the given
     * frame data.
     * 
     * @param thread
     * @param id
     *      stack frame id (1 is the bottom of the stack)
     * @param keywordName
     * @param keywordContext
     * 
     */
    public RobotStackFrame(final RobotThread thread, final int id, final String keywordName, final KeywordContext keywordContext) {
        super(thread.getDebugTarget());
        this.thread = thread;
        setStackFrameData(id, keywordName, keywordContext);
    }
    
    public void setStackFrameData(final int id, final String keywordName, final KeywordContext keywordContext) {
        this.stackLevel = id;
        this.fileName = keywordContext.getFileName();
        this.lineNumber = keywordContext.getLineNumber();
        this.name = keywordName + " [line:" + keywordContext.getLineNumber() + "]";
        initVariables(keywordContext.getVariables());
    }

    private void initVariables(final Map<String, Object> vars) {
        if (vars != null) {
            variables = thread.getDebugTarget().getRobotVariablesManager()
                    .extractRobotDebugVariables(stackLevel, vars);
        }
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

    public int getStackLevel() {
        return stackLevel;
    }

    @Override
    public RobotThread getThread() {
        return thread;
    }

    @Override
    public IVariable[] getVariables() {
        return variables == null ? new IVariable[0] : variables;
    }

    // gets nested variables too
    public List<IVariable> getAllVariables() {
        final List<IVariable> vars = newArrayList(getVariables());
        for (final IVariable var : variables) {
            final RobotDebugVariable robotVar = (RobotDebugVariable) var;
            final RobotDebugValue value = robotVar.getValue();

            vars.addAll(newArrayList(value.getVariables()));
        }
        return vars;
    }

    @Override
    public boolean hasVariables() {
        return variables != null && variables.length > 0;
    }

    @Override
    public int getLineNumber() {
        return lineNumber;
    }

    @Override
    public int getCharStart() {
        return -1;
    }

    @Override
    public int getCharEnd() {
        return -1;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public IRegisterGroup[] getRegisterGroups() {
        return null;
    }

    @Override
    public boolean hasRegisterGroups() {
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
    public void stepInto() {
        getThread().stepInto();
    }

    @Override
    public void stepOver() {
        getThread().stepOver();
    }

    @Override
    public void stepReturn() {
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
    public void resume() {
        getThread().resume();
    }

    @Override
    public void suspend() {
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
    public void terminate() {
        getThread().terminate();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof RobotStackFrame) {
            final RobotStackFrame that = (RobotStackFrame) obj;
            return that.getSourceName().equals(this.getSourceName()) && that.getLineNumber() == this.getLineNumber()
                    && that.stackLevel == stackLevel;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSourceName(), stackLevel);
    }
}
