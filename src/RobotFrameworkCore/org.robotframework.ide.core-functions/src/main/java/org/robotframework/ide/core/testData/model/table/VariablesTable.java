package org.robotframework.ide.core.testData.model.table;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.table.variables.AVariable;


public class VariablesTable extends ATableModel {

    @SuppressWarnings("rawtypes")
    public List<AVariable> variables = new LinkedList<>();


    public VariablesTable(final TableHeader header) {
        super(header, "Variables");
    }
}
