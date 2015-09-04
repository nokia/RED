package org.robotframework.ide.eclipse.main.plugin.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.robotframework.ide.core.testData.model.table.ARobotSectionTable;
import org.robotframework.ide.core.testData.model.table.VariableTable;
import org.robotframework.ide.core.testData.model.table.variables.IVariableHolder;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable.Type;

public class RobotVariablesSection extends RobotSuiteFileSection {

    public static final String SECTION_NAME = "Variables";
    
    private VariableTable modelTable;

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
        
        if(variableType == Type.SCALAR) {
            modelTable.createScalarVariable(index, name, Arrays.asList(value), comment);
        } else if(variableType == Type.LIST) {
            modelTable.createListVariable(index, name, createList(value), comment);
        } if(variableType == Type.DICTIONARY) {
            modelTable.createDictionaryVariable(index, name, createDict(value), comment);
        }
        
        return robotVariable;
    }
    
    @Override
    public void link(final ARobotSectionTable table) {
        final VariableTable varTable = (VariableTable) table;
        modelTable = varTable;
        for (final IVariableHolder variableHolder : varTable.getVariables()) {
            final RobotVariable variable = new RobotVariable(this);
            variable.link(variableHolder);
            elements.add(variable);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<RobotVariable> getChildren() {
        return (List<RobotVariable>) super.getChildren();
    }
    
    private List<String> createList(final String value) {
        final String[] values = value.split("(\\s{2,}|\t)"); 
        return Arrays.asList(values);
    }
    
    private Map<String, String> createDict(final String value) {
        String[] keyValuePair;
        final Map<String, String> map = new HashMap<String, String>();
        final String[] values = value.split("(\\s{2,}|\t)"); 
        for (int i = 0; i < values.length; i++) {
            if (!values[i].equals("") && values[i].contains("=")) {
                keyValuePair = values[i].split("=");
                if (keyValuePair.length == 2) {
                    map.put(keyValuePair[0], keyValuePair[1]);
                } else if (keyValuePair.length == 1) {
                    map.put(keyValuePair[0], "");
                } else {
                    map.put("", "");
                }
            }
        }
        return map;
    }
    
}
