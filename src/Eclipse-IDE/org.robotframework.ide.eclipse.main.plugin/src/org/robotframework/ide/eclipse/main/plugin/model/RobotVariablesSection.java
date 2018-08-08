/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.rf.ide.core.testdata.model.table.VariableTable;
import org.rf.ide.core.testdata.model.table.variables.AVariable;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableType;

public class RobotVariablesSection extends RobotSuiteFileSection {

    public static final String SECTION_NAME = "Variables";

    RobotVariablesSection(final RobotSuiteFile parent, final VariableTable variableTable) {
        super(parent, SECTION_NAME, variableTable);
    }

    @Override
    public void link() {
        for (final AVariable variableHolder : getLinkedElement().getVariables()) {
            final RobotVariable variable = new RobotVariable(this, variableHolder);
            elements.add(variable);
        }
    }

    @Override
    public VariableTable getLinkedElement() {
        return (VariableTable) super.getLinkedElement();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<RobotVariable> getChildren() {
        return (List<RobotVariable>) super.getChildren();
    }

    @Override
    public String getDefaultChildName() {
        return "var";
    }

    @Override
    public RobotFileInternalElement createChild(final int index, final String name) {
        VariableType actualType = VariableType.INVALID;
        for (final VariableType type : EnumSet.complementOf(EnumSet.of(VariableType.INVALID))) {
            if (name.startsWith(type.getIdentificator())) {
                actualType = type;
                break;
            }
        }
        String actualName = actualType == VariableType.INVALID ? name : name.substring(1);
        actualName = actualName.startsWith("{") ? actualName.substring(1) : actualName;
        actualName = actualName.endsWith("}") ? actualName.substring(0, actualName.length() - 1) : actualName;

        return createVariable(index, actualType, actualName);
    }

    @Override
    public void insertChild(final int index, final RobotFileInternalElement element) {
        throw new IllegalStateException("Not implemented for variables section");
    }

    @Override
    public void removeChildren(final List<? extends RobotFileInternalElement> elementsToRemove) {
        throw new IllegalStateException("Not implemented for variables section");
    }

    public RobotVariable createVariable(final int index, final VariableType variableType, final String name) {
        final int actualIndex = 0 <= index && index < elements.size() ? index : elements.size();

        AVariable var;
        if (variableType == VariableType.SCALAR) {
            var = getLinkedElement().createScalarVariable(actualIndex, name, new ArrayList<>());
        } else if (variableType == VariableType.LIST) {
            var = getLinkedElement().createListVariable(actualIndex, name, new ArrayList<>());
        } else if (variableType == VariableType.DICTIONARY) {
            var = getLinkedElement().createDictionaryVariable(actualIndex, name, new ArrayList<>());
        } else {
            throw new IllegalArgumentException("Unable to create variable of type " + variableType.name());
        }

        final RobotVariable robotVariable = new RobotVariable(this, var);
        elements.add(actualIndex, robotVariable);
        return robotVariable;
    }

    public void addVariable(final RobotVariable variable) {
        addVariable(elements.size(), variable);
    }

    public void addVariable(final int index, final RobotVariable variable) {
        variable.setParent(this);
        elements.add(index, variable);
        getLinkedElement().addVariable(index, variable.getLinkedElement());
    }
}