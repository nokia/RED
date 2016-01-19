/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.read.recognizer.header;

import java.util.regex.Pattern;

import org.rf.ide.core.testdata.text.read.recognizer.ATokenRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class VariablesTableHeaderRecognizer extends ATokenRecognizer {

    public static final Pattern EXPECTED = Pattern
            .compile("[ ]?([*][\\s]*)+[\\s]*(" + createUpperLowerCaseWordWithSpacesInside("Variables") + "|"
                    + createUpperLowerCaseWordWithSpacesInside("Variable") + ")([\\s]*[*])*");

    public VariablesTableHeaderRecognizer() {
        super(EXPECTED, RobotTokenType.VARIABLES_TABLE_HEADER);
    }

    @Override
    public ATokenRecognizer newInstance() {
        return new VariablesTableHeaderRecognizer();
    }
}
