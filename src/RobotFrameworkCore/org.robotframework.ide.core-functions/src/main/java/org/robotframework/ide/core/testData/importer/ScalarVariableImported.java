package org.robotframework.ide.core.testData.importer;

import org.robotframework.ide.core.testData.model.table.variables.AVariable.VariableType;


public class ScalarVariableImported extends AVariableImported<String> {

    public ScalarVariableImported(String name) {
        super(name, VariableType.SCALAR);
    }
}
