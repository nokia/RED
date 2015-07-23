package org.robotframework.ide.eclipse.main.plugin.model;

import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable.Type;

public class RobotVariablesSection extends RobotSuiteFileSection {

    public static final String SECTION_NAME = "Variables";

    public RobotVariablesSection(final RobotSuiteFile parent, final boolean readOnly) {
        super(parent, SECTION_NAME, readOnly);
    }

    public RobotVariable createListVariable(final String name, final String value, final String comment) {
        final RobotVariable robotVariable = new RobotVariable(this, Type.LIST, name, value, comment);
        elements.add(robotVariable);
        return robotVariable;
    }
    
    public RobotVariable createDictionaryVariable(final String name, final String value, final String comment) {
        final RobotVariable robotVariable = new RobotVariable(this, Type.DICTIONARY, name, value, comment);
        elements.add(robotVariable);
        return robotVariable;
    }

    public RobotVariable createScalarVariable(final String name, final String value, final String comment) {
        final RobotVariable robotVariable = new RobotVariable(this, Type.SCALAR, name, value, comment);
        elements.add(robotVariable);
        return robotVariable;
    }

}
