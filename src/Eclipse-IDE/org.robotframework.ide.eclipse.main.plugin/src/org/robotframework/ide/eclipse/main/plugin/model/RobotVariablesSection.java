package org.robotframework.ide.eclipse.main.plugin.model;

import java.util.List;

import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable.Type;

public class RobotVariablesSection extends RobotSuiteFileSection {

    public static final String SECTION_NAME = "Variables";

    RobotVariablesSection(final RobotSuiteFile parent) {
        super(parent, SECTION_NAME);
    }

    public RobotVariable createScalarVariable(final String name, final String value, final String comment) {
        return createVariable(Type.SCALAR, name, value, comment);
    }

    public RobotVariable createListVariable(final String name, final String value, final String comment) {
        return createVariable(Type.LIST, name, value, comment);
    }
    
    public RobotVariable createDictionaryVariable(final String name, final String value, final String comment) {
        return createVariable(Type.DICTIONARY, name, value, comment);
    }

    public RobotVariable createVariable(final Type variableType, final String name, final String value,
            final String comment) {
        return createVariable(getChildren().size(), variableType, name, value, comment);
    }

    public RobotVariable createVariable(final int index, final Type variableType, final String name,
            final String value, final String comment) {
        final RobotVariable robotVariable = new RobotVariable(this, variableType, name, value, comment);
        elements.add(index, robotVariable);
        return robotVariable;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<RobotVariable> getChildren() {
        return (List<RobotVariable>) super.getChildren();
    }
}
