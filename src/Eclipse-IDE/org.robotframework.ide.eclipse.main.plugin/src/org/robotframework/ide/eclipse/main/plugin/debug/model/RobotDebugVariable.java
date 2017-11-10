/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.debug.model;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.rf.ide.core.execution.debug.StackFrameVariable;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableScope;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.base.Splitter;

/**
 * @author mmarzec
 */
public class RobotDebugVariable extends RobotDebugElement implements IVariable {

    static final String AUTOMATIC_NAME = "Automatic Variables";

    private final RobotStackFrame frame;
    private final RobotDebugVariable parent;
    private final RobotDebugValue value;
    private boolean valueChanged;

    private final String name;
    private final StackFrameVariable stackVariable;

    private final boolean isArtificial;

    static Comparator<RobotDebugVariable> sorter() {
        return (var1, var2) -> extractVariableName(var1.name).compareToIgnoreCase(extractVariableName(var2.name));
    }

    private static String extractVariableName(final String name) {
        return name.matches("^[$@&%]\\{.+\\}$") ? name.substring(2, name.length() - 1) : name;
    }

    public RobotDebugVariable(final RobotStackFrame frame, final StackFrameVariable stackVariable) {
        super(frame.getDebugTarget());
        this.frame = frame;
        this.parent = null;
        this.value = RobotDebugValue.createFromValue(this, stackVariable.getType(), stackVariable.getValue());
        this.valueChanged = false;

        this.name = stackVariable.getName();
        this.stackVariable = stackVariable;

        this.isArtificial = false;
    }

    RobotDebugVariable(final RobotDebugVariable parent, final String name, final String type, final Object value) {
        super(parent.getDebugTarget());
        this.frame = parent.frame;
        this.parent = parent;
        this.value = RobotDebugValue.createFromValue(this, type, value);
        this.valueChanged = false;

        this.name = name;
        this.stackVariable = null;

        this.isArtificial = false;
    }

    private RobotDebugVariable(final RobotStackFrame frame, final RobotDebugValue value) {
        super(frame.getDebugTarget());
        this.frame = frame;
        this.parent = null;
        this.value = value;
        this.valueChanged = false;

        this.name = AUTOMATIC_NAME;
        this.stackVariable = null;

        this.isArtificial = true;
    }

    @VisibleForTesting
    public static RobotDebugVariable createAutomatic(final RobotStackFrame frame,
            final List<RobotDebugVariable> automaticVars) {
        final RobotDebugValue automaticVarsValue = new RobotDebugValueOfDictionary(frame.getDebugTarget(), "", "",
                automaticVars);
        return new RobotDebugVariable(frame, automaticVarsValue);
    }

    RobotDebugVariable getParent() {
        return parent;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getReferenceTypeName() {
        return "";
    }

    @Override
    public boolean hasValueChanged() {
        return valueChanged;
    }

    @Override
    public RobotDebugValue getValue() {
        return value;
    }

    public boolean isArtificial() {
        return isArtificial;
    }

    private boolean isTopLevel() {
        return !isArtificial() && stackVariable != null;
    }

    @Override
    public boolean supportsValueModification() {
        return !isArtificial();
    }

    @Override
    public boolean verifyValue(final String expression) {
        return true;
    }

    @Override
    public void setValue(final String expression) {
        final List<String> arguments = extractArguments(expression.replaceAll("\\\\", "\\\\\\\\"));
        if (isTopLevel()) {
            changeVariable(arguments);
        } else {
            changeVariableInnerValue(arguments);
        }
        setValueChanged(true);
        frame.fireResumeEvent(DebugEvent.EVALUATION);
    }

    private List<String> extractArguments(final String expression) {
        if (value instanceof RobotDebugValueOfScalar) {
            return newArrayList(expression);

        } else if ((value instanceof RobotDebugValueOfList && expression.startsWith("[")
                        && expression.endsWith("]"))
                || (value instanceof RobotDebugValueOfDictionary && expression.startsWith("{")
                        && expression.endsWith("}"))) {
            return Splitter.on(',')
                    .splitToList(expression.substring(1, expression.length() - 1))
                    .stream()
                    .map(String::trim)
                    .collect(toList());
        }
        return newArrayList(expression.split("\\s{2,}|\t"));
    }

    private void changeVariable(final List<String> arguments) {
        frame.changeVariable(stackVariable, arguments);
    }

    private void changeVariableInnerValue(final List<String> arguments) {
        final Object f = newArrayList(typeIdentifierOf(this), null);
        final List<Object> path = newArrayList(f);
        RobotDebugVariable current = this;
        while (current != null) {
            if (current.stackVariable != null) {
                frame.changeVariableInnerValue(current.stackVariable, path, arguments);
                return;
            }
            path.add(0, newArrayList(typeIdentifierOf(current.parent), extractIndexOrKey(current.name)));
            current = current.parent;
        }
        throw new IllegalStateException("Every non-artificial IVariable has to have real variable in some predecessor");
    }

    private static String typeIdentifierOf(final RobotDebugVariable variable) {
        if (variable.value instanceof RobotDebugValueOfDictionary) {
            return "dict";

        } else if (variable.value instanceof RobotDebugValueOfList) {
            return "list";

        } else if (variable.value instanceof RobotDebugValueOfScalar) {
            return "scalar";

        } else {
            throw new IllegalStateException("Unrecognized type of variable value");
        }
    }

    private static Object extractIndexOrKey(final String name) {
        if (name.startsWith("[") && name.endsWith("]")) {
            return Integer.valueOf(name.substring(1, name.length() - 1));
        }
        return name;
    }

    @Override
    public boolean verifyValue(final IValue value) {
        return false;
    }

    @Override
    public void setValue(final IValue newValue) throws DebugException {
        throw new DebugException(new Status(IStatus.ERROR, RedPlugin.PLUGIN_ID, DebugException.NOT_SUPPORTED,
                "Variables can be only edited using string expressions", null));
    }

    void setValueChanged(final boolean valueChanged) {
        this.valueChanged = valueChanged;
    }

    void visitAllVariables(final RobotDebugVariableVisitor visitor) {
        visitor.visit(this);
        value.visitAllVariables(visitor);
    }

    public ImageDescriptor getImage() {
        if (isArtificial) {
            return RedImages.getElementImage();
        } else if (value instanceof RobotDebugValueOfScalar) {
            return RedImages.VARIABLES.getDebugScalarVariableImage();
        } else if (value instanceof RobotDebugValueOfList) {
            return RedImages.VARIABLES.getDebugListVariableImage();
        } else if (value instanceof RobotDebugValueOfDictionary) {
            return RedImages.VARIABLES.getDebugDictionaryVariableImage();
        } else {
            return null;
        }
    }

    public Optional<VariableScope> getScope() {
        return Optional.ofNullable(stackVariable).map(StackFrameVariable::getScope);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof RobotDebugVariable) {
            final RobotDebugVariable that = (RobotDebugVariable) obj;
            return Objects.equal(this.stackVariable, that.stackVariable) && this.name.equals(that.name)
                    && this.parent == that.parent;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(stackVariable, name, parent);
    }

    @FunctionalInterface
    public static interface RobotDebugVariableVisitor {

        void visit(RobotDebugVariable variable);
    }
}
