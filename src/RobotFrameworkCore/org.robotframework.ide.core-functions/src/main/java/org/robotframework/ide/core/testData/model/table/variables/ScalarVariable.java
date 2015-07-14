package org.robotframework.ide.core.testData.model.table.variables;

import org.robotframework.ide.core.testData.model.common.Text;


public class ScalarVariable extends AVariable<Text> {

    private final ScalarName scalarName;


    public ScalarVariable(final ScalarName scalarName) {
        super(VariableType.SCALAR);
        this.scalarName = scalarName;
    }
}
