/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.read.recognizer.variables;

import java.util.regex.Pattern;

import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableType;
import org.rf.ide.core.testdata.text.read.recognizer.ATokenRecognizer;

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
