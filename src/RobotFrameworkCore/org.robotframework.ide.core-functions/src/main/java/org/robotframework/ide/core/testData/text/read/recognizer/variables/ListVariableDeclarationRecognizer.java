package org.robotframework.ide.core.testData.text.read.recognizer.variables;

import org.robotframework.ide.core.testData.model.table.variables.AVariable.VariableType;


public class ListVariableDeclarationRecognizer extends
        AVariablesTokenRecognizer {

    public ListVariableDeclarationRecognizer() {
        super(VariableType.LIST);
    }
}
