package org.robotframework.ide.core.testData.model.table.variables;

import org.robotframework.ide.core.testData.model.common.Text;


public class ListVariable extends AVariable<Text> {

    private final ListName listName;


    public ListVariable(final ListName listName) {
        super(VariableType.LIST);
        this.listName = listName;
    }
}
