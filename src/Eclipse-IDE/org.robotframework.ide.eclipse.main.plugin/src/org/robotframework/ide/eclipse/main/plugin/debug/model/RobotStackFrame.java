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
import org.eclipse.debug.core.model.IRegisterGroup;
import org.eclipse.debug.core.model.IStackFrame;
import org.rf.ide.core.execution.debug.StackFrame;
import org.rf.ide.core.execution.debug.StackFrameVariable;
import org.rf.ide.core.execution.debug.StackFrameVariables;
import org.rf.ide.core.execution.debug.StackFrameVariables.StackVariablesDelta;
import org.rf.ide.core.execution.debug.UserProcessDebugController;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.FileRegion;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Streams;


public class RobotStackFrame extends RobotDebugElement implements IStackFrame {

    private final RobotThread thread;

    private final StackFrame frame;

    private final UserProcessDebugController userController;


    @VisibleForTesting
    public RobotStackFrame(final RobotThread thread, final StackFrame frame,
            final UserProcessDebugController userController) {
        super(thread.getDebugTarget());
        this.thread = thread;
        this.frame = frame;
        this.userController = userController;
    }

    @VisibleForTesting
    StackFrame getFrame() {
        return frame;
    }

    public Optional<URI> getPath() {
        return frame.getCurrentSourcePath();
    }

    public Optional<URI> getContextPath() {
        return frame.getContextPath();
    }

    public boolean isErroneous() {
        return frame.isErroneous();
    }

    public boolean isLibraryKeywordFrame() {
        return frame.isLibraryKeywordFrame();
    }

    public boolean isSuiteDirectoryContext() {
        return frame.isSuiteDirectoryContext();
    }

    public boolean isSuiteFileContext() {
        return frame.isSuiteFileContext();
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
            thread.resumed();
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
            thread.resumed();
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
            thread.resumed();
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
        return isSuspended();
    }

    @Override
    public synchronized RobotDebugVariable[] getVariables() {
        final Map<String, RobotDebugVariable> variables = createVariables(frame.getVariables());
        if (frame.getLastDelta().isPresent()) {
            markChanges(variables, frame.getLastDelta().get());
        }
        return variables.values().toArray(new RobotDebugVariable[0]);
    }

    Map<String, RobotDebugVariable> createVariables(final StackFrameVariables variables) {
        final List<RobotDebugVariable> nonAutomaticVariables = Streams.stream(variables)
                .filter(not(StackFrameVariable::isAutomatic))
                .map(var -> new RobotDebugVariable(this, var))
                .collect(toList());

        final List<RobotDebugVariable> automaticVars = Streams.stream(variables)
                .filter(StackFrameVariable::isAutomatic)
                .map(var -> new RobotDebugVariable(this, var))
                .collect(toList());

        final Map<String, RobotDebugVariable> vars = new LinkedHashMap<>();
        for (final RobotDebugVariable var : nonAutomaticVariables) {
            vars.put(var.getName(), var);
        }
        final RobotDebugVariable automatic = RobotDebugVariable.createAutomatic(this, automaticVars);
        vars.put(automatic.getName(), automatic);
        return vars;
    }

    private void markChanges(final Map<String, RobotDebugVariable> variables,
            final StackVariablesDelta delta) {

        final RobotDebugVariable automaticGroup = variables.get(RobotDebugVariable.AUTOMATIC_NAME);

        final Function<StackFrameVariable, RobotDebugVariable> variablesSelector = variable -> {
            if (variable.isAutomatic()) {
                return automaticGroup.getValue().getVariable(variable.getName());
            } else {
                return variables.get(variable.getName());
            }
        };

        for (final StackFrameVariable variable : frame.getVariables()) {
            final String varName = variable.getName();
            final boolean shouldMark = delta.isChanged(varName) || delta.isAdded(varName);

            final RobotDebugVariable var = variablesSelector.apply(variable);
            var.setValueChanged(shouldMark);
            if (variable.isAutomatic() && shouldMark) {
                automaticGroup.setValueChanged(true);
            }
        }
    }

    public List<? extends RobotDebugVariable> getAllVariables() {
        final List<RobotDebugVariable> allVariables = new ArrayList<>();
        for (final RobotDebugVariable var : getVariables()) {
            var.visitAllVariables(v -> allVariables.add(v));
        }
        return allVariables;
    }

    public void changeVariable(final StackFrameVariable stackVariable, final List<String> arguments) {
        getDebugTarget().changeVariable(frame, stackVariable, arguments);
    }

    public void changeVariableInnerValue(final StackFrameVariable variable, final List<Object> path,
            final List<String> arguments) {
        getDebugTarget().changeVariableInnerValue(frame, variable, path, arguments);
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
    public boolean hasRegisterGroups() {
        return false;
    }

    @Override
    public IRegisterGroup[] getRegisterGroups() {
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
