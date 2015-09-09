/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see licence.txt file for details.
 */
package org.robotframework.ide.core.testData.text.read.recognizer.variables;

import org.robotframework.ide.core.testData.model.table.variables.AVariable.VariableType;


public class ListVariableDeclarationRecognizer extends
        AVariablesTokenRecognizer {

    public ListVariableDeclarationRecognizer() {
        super(VariableType.LIST);
    }
}
