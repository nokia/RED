/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.read.recognizer.settings;

import java.util.regex.Pattern;

import org.rf.ide.core.testdata.text.read.recognizer.ATokenRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;


public class VariableDeclarationRecognizer extends ATokenRecognizer {

    public static final Pattern EXPECTED = Pattern.compile("[ ]?("
            + createUpperLowerCaseWord("Variables") + "[\\s]*:" + "|"
            + createUpperLowerCaseWord("Variables") + ")");


    public VariableDeclarationRecognizer() {
        super(EXPECTED, RobotTokenType.SETTING_VARIABLES_DECLARATION);
    }


    @Override
    public ATokenRecognizer newInstance() {
        return new VariableDeclarationRecognizer();
    }
}
