/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.debug.model;

import static com.google.common.base.Predicates.not;
import static java.util.stream.Collectors.toList;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IRegisterGroup;
import org.eclipse.debug.core.model.IStackFrame;
import org.rf.ide.core.execution.debug.StackFrame;
import org.rf.ide.core.execution.debug.StackFrameVariable;
import org.rf.ide.core.execution.debug.StackFrameVariables.StackVariablesDelta;
import org.rf.ide.core.execution.debug.UserProcessDebugController;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.FileRegion;

import com.google.common.collect.Streams;


public class RobotStackFrame extends RobotDebugElement implements IStackFrame {

    private final RobotThread thread;

    private final StackFrame frame;

    private final UserProcessDebugController userController;

    private Map<String, RobotDebugVariable> variables;

    protected RobotStackFrame(final RobotThread thread, final StackFrame frame,
            final UserProcessDebugController userController) {
        super(thread.getDebugTarget());
        this.thread = thread;
        this.frame = frame;
        this.userController = userController;
        this.variables = new LinkedHashMap<>();

        this.frame.addVariablesChangesListener(delta -> updateVariables(delta));
        this.frame.addContextChangesListener(() -> this.fireChangeEvent(DebugEvent.CONTENT));
    }

    void createVariables() {
        final List<RobotDebugVariable> nonAutomaticVariables = Streams.stream(frame.getVariables())
                .filter(not(StackFrameVariable::isAutomatic))
                .map(var -> new RobotDebugVariable(this, var))
                .collect(toList());

        final List<RobotDebugVariable> automaticVars = Streams.stream(frame.getVariables())
                .filter(StackFrameVariable::isAutomatic)
                .map(var -> new RobotDebugVariable(this, var))
                .collect(toList());

        for (final RobotDebugVariable var : nonAutomaticVariables) {
            this.variables.put(var.getName(), var);
        }
        final RobotDebugVariable automatic = RobotDebugVariable.createAutomatic(this, automaticVars);
        this.variables.put(automatic.getName(), automatic);
    }

    private void updateVariables(final StackVariablesDelta delta) {
        final RobotDebugVariable automaticGroup = variables.get(RobotDebugVariable.AUTOMATIC_NAME);
        
        final Function<StackFrameVariable, RobotDebugVariable> variablesSupplier = variable -> {
            if (variable.isAutomatic()) {
                return automaticGroup.getValue().getVariable(variable.getName());
            } else {
                return variables.get(variable.getName());
            }
        };
        
        final Map<String, RobotDebugVariable> newAutomaticVariables = new LinkedHashMap<>();
        final Map<String, RobotDebugVariable> newVariables = new LinkedHashMap<>();

        for (final StackFrameVariable variable : frame.getVariables()) {
            final Map<String, RobotDebugVariable> targetMapping = variable.isAutomatic() ? newAutomaticVariables
                    : newVariables;
            final String varName = variable.getName();

            RobotDebugVariable currentVar = variablesSupplier.apply(variable);

            if (delta.isUnchanged(varName)) {
                currentVar.setValueChanged(false);
                targetMapping.put(varName, currentVar);

            } else if (delta.isChanged(varName)) {
                currentVar.setValueChanged(true);
                if (variable.isAutomatic()) {
                    automaticGroup.setValueChanged(true);
                }
                currentVar.syncValue(varName, variable.getType(), variable.getValue());
                targetMapping.put(varName, currentVar);

            } else if (delta.isAdded(varName)) {
                currentVar = new RobotDebugVariable(this, variable);
                currentVar.setValueChanged(true);
                if (variable.isAutomatic()) {
                    automaticGroup.setValueChanged(true);
                }
                targetMapping.put(varName, currentVar);
            }
            // otherwise it was removed, so we won't add it to new map
        }
        automaticGroup.syncAutomaticValue(newAutomaticVariables);
        newVariables.put(RobotDebugVariable.AUTOMATIC_NAME, automaticGroup);
        this.variables = newVariables;
    }

    public StackFrame getFrame() {
        return frame;
    }

    public Optional<URI> getPath() {
        return frame.getCurrentSourcePath();
    }

    public Optional<URI> getContextPath() {
        return frame.getContextPath();
    }

    public String getLabel() {
        String prefix;
        if (frame.isSuiteDirectoryContext() || frame.isSuiteFileContext()) {
            prefix = "[Suite] ";
        } else if (frame.isTestContext()) {
            prefix = "[Test] ";
        } else {
            prefix = "";
        }

        final int lineNo = getLineNumber();
        return prefix + frame.getName() + (lineNo == -1 ? "" : " [line: " + lineNo + "]");
    }

    public String getInstructionPointerText() {
        if (frame.isErroneous()) {
            return frame.getErrorMessage();
        }
        return "";
    }

    public boolean isTopFrame() {
        return this.equals(thread.getTopStackFrame());
    }

    @Override
    public boolean canStepInto() {
        return isSuspended() && isTopFrame();
    }

    @Override
    public void stepInto() {
        userController.stepInto(() -> {
            getDebugTarget().getProcess().resumed();
            getThread().fireResumeEvent(DebugEvent.STEP_INTO);
        }, () -> {
            getThread().fireSuspendEvent(DebugEvent.STEP_END);
        });
    }

    @Override
    public boolean canStepOver() {
        return isSuspended();
    }

    @Override
    public void stepOver() {
        userController.stepOver(frame, () -> {
            getDebugTarget().getProcess().resumed();
            getThread().fireResumeEvent(DebugEvent.STEP_OVER);
        }, () -> {
            getThread().fireSuspendEvent(DebugEvent.STEP_END);
        });
    }

    @Override
    public boolean canStepReturn() {
        return isSuspended();
    }

    @Override
    public void stepReturn() {
        userController.stepReturn(frame, () -> {
            getDebugTarget().getProcess().resumed();
            getThread().fireResumeEvent(DebugEvent.STEP_RETURN);
        }, () -> {
            getThread().fireSuspendEvent(DebugEvent.STEP_END);
        });
    }

    @Override
    public boolean isStepping() {
        return getThread().isStepping();
    }

    @Override
    public RobotThread getThread() {
        return thread;
    }

    @Override
    public boolean hasVariables() {
        return getThread().isSuspended() && !variables.isEmpty();
    }

    @Override
    public RobotDebugVariable[] getVariables() {
        return variables.values().toArray(new RobotDebugVariable[0]);
    }

    public List<? extends RobotDebugVariable> getAllVariables() {
        final List<RobotDebugVariable> allVariables = new ArrayList<>();
        for (final RobotDebugVariable var : this.variables.values()) {
            var.visitAllVariables(v -> allVariables.add(v));
        }
        return allVariables;
    }

    @Override
    public int getLineNumber() {
        return frame.getFileRegion().map(FileRegion::getStart).map(FilePosition::getLine).orElse(-1);
    }

    @Override
    public int getCharStart() {
        return frame.getFileRegion().map(FileRegion::getStart).map(FilePosition::getOffset).orElse(-1);
    }

    @Override
    public int getCharEnd() {
        return frame.getFileRegion().map(FileRegion::getEnd).map(FilePosition::getOffset).orElse(-1);
    }

    @Override
    public String getName() {
        return frame.getName();
    }

    @Override
    public boolean hasRegisterGroups() throws DebugException {
        return false;
    }

    @Override
    public IRegisterGroup[] getRegisterGroups() throws DebugException {
        return null;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj != null && obj.getClass() == RobotStackFrame.class) {
            final RobotStackFrame that = (RobotStackFrame) obj;
            return this.thread == that.thread && this.frame == that.frame;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return frame.hashCode();
    }
}
