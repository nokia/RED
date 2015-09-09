/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.text.read.recognizer.variables;

import org.robotframework.ide.core.testData.model.table.variables.AVariable.VariableType;


public class ScalarVariableDeclarationRecognizer extends
        AVariablesTokenRecognizer {

    public ScalarVariableDeclarationRecognizer() {
        super(VariableType.SCALAR);
    }
}
