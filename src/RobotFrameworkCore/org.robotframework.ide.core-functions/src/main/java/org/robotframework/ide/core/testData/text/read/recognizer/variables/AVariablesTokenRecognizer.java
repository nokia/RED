/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.text.read.recognizer.variables;

import java.util.regex.Pattern;

import org.robotframework.ide.core.testData.model.table.variables.AVariable.VariableType;
import org.robotframework.ide.core.testData.text.read.recognizer.ATokenRecognizer;

import com.google.common.annotations.VisibleForTesting;


public abstract class AVariablesTokenRecognizer extends ATokenRecognizer {

    private static final String TEMPLATE = "[ ]?[" + "%s" + "]"
            + "(\\s*)[{].*([}]$|$)";


    protected AVariablesTokenRecognizer(final VariableType varType) {
        super(createVariablePattern(varType), varType.getType());
    }


    @VisibleForTesting
    protected static Pattern createVariablePattern(final VariableType varType) {
        return Pattern.compile(String.format(TEMPLATE,
                varType.getIdentificator()));
    }
}
