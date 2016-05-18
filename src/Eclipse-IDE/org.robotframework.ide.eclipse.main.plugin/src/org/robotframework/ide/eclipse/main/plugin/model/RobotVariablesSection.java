/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model;

import java.util.List;
import java.util.Map.Entry;

import org.rf.ide.core.testdata.model.table.ARobotSectionTable;
import org.rf.ide.core.testdata.model.table.VariableTable;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableType;
import org.rf.ide.core.testdata.model.table.variables.IVariableHolder;

import com.google.common.collect.Lists;

public class RobotVariablesSection extends RobotSuiteFileSection {

    public static final String SECTION_NAME = "Variables";

    RobotVariablesSection(final RobotSuiteFile parent) {
        super(parent, SECTION_NAME);
    }

    private VariableTable getTable() {
        return (VariableTable) super.sectionTable;
    }

    public RobotVariable createVariable(final VariableType variableType, final String name) {
        return createVariable(getChildren().size(), variableType, name);
    }

    public RobotVariable createVariable(final int index, final VariableType variableType, final String name) {
        if (variableType == VariableType.SCALAR) {
            getTable().createScalarVariable(index, name, Lists.<String> newArrayList());
        } else if (variableType == VariableType.LIST) {
            getTable().createListVariable(index, name, Lists.<String> newArrayList());
        } else if (variableType == VariableType.DICTIONARY) {
            getTable().createDictionaryVariable(index, name, Lists.<Entry<String, String>> newArrayList());
        }

        final IVariableHolder newVariableHolder = getTable().getVariables().get(index);
        final RobotVariable robotVariable = new RobotVariable(this, newVariableHolder);
        elements.add(index, robotVariable);

        return robotVariable;
    }

    @Override
    public void link(final ARobotSectionTable table) {
        super.link(table);

        for (final IVariableHolder variableHolder : getTable().getVariables()) {
            final RobotVariable variable = new RobotVariable(this, variableHolder);
            elements.add(variable);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<RobotVariable> getChildren() {
        return (List<RobotVariable>) super.getChildren();
    }
}
