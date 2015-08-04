package org.robotframework.ide.core.testData.model.table;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.table.variables.IVariableHolder;


public class VariableTable extends ARobotSectionTable {

    private List<IVariableHolder> variables = new LinkedList<>();


    public List<IVariableHolder> getVariables() {
        return variables;
    }


    public void addVariable(final IVariableHolder variable) {
        variables.add(variable);
    }
}
